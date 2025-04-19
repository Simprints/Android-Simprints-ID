package com.simprints.infra.enrolment.records.repository.local

import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.models.toDomain
import com.simprints.infra.enrolment.records.repository.local.models.toRoomDb
import com.simprints.infra.enrolment.records.room.store.SubjectDao
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabaseFactory
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import com.simprints.infra.enrolment.records.room.store.models.DbSubjectTemplateFormatMap
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.simprints.infra.enrolment.records.repository.domain.models.Subject as SubjectDomain

@Singleton
internal class RoomEnrolmentRecordLocalDataSource @Inject constructor(
    private val subjectsDatabaseFactory: SubjectsDatabaseFactory,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
    private val tokenizationProcessor: TokenizationProcessor,
) : EnrolmentRecordLocalDataSource {
    val database: SubjectsDatabase by lazy { subjectsDatabaseFactory.get() }
    val subjectDao: SubjectDao by lazy {
        subjectsDatabaseFactory.get().subjectDao
    }

    override suspend fun load(query: SubjectQuery): List<SubjectDomain> = withContext(dispatcherIO) {
        if (query.hasUntokenizedFields == false) {
            return@withContext emptyList() // All records inserted in this database are tokenized
        }
        val sqlQuery = buildSubjectQuery(query)
        subjectDao.loadSubjects(SimpleSQLiteQuery(sqlQuery.first, sqlQuery.second.toTypedArray())).map { it.toDomain() }
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = withContext(dispatcherIO) {
        val sqlQuery = buildCountQuery(query)
        subjectDao.count(SimpleSQLiteQuery(sqlQuery.first, sqlQuery.second.toTypedArray()))
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ): List<FingerprintIdentity> = withContext(dispatcherIO) {
        require(query.fingerprintSampleFormat != null) { "fingerprintSampleFormat is required" }
        subjectDao
            .loadFingerprintSamples(
                query.fingerprintSampleFormat,
                query.projectId,
                query.subjectId,
                query.attendantId?.value,
                query.moduleId?.value,
                offset = range.first,
                limit = range.last - range.first,
            ).map {
                onCandidateLoaded()
                FingerprintIdentity(
                    subjectId = it.key,
                    fingerprints = it.value.map { sample ->
                        FingerprintSample(
                            fingerIdentifier = IFingerIdentifier.entries[sample.fingerIdentifier],
                            template = sample.template,
                            id = "",
                            format = query.fingerprintSampleFormat,
                            referenceId = "",
                        )
                    },
                )
            }
    }

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ): List<FaceIdentity> = withContext(dispatcherIO) {
        require(query.faceSampleFormat != null) { "faceSampleFormat is required" }

        subjectDao
            .loadFaceSamples(
                query.faceSampleFormat,
                query.projectId,
                query.subjectId,
                query.attendantId?.value,
                query.moduleId?.value,
                offset = range.first,
                limit = range.last - range.first,
            ).map { item ->
                onCandidateLoaded()
                // Matcher only cares about the template
                FaceIdentity(
                    subjectId = item.key,
                    faces = item.value.map {
                        FaceSample(
                            template = it,
                            id = "",
                            format = query.faceSampleFormat,
                            referenceId = "",
                        )
                    },
                )
            }
    }

    private fun buildSubjectQuery(query: SubjectQuery): Pair<String, List<Any?>> {
        val (whereClause, args) = buildWhereClause(query)
        val orderBy = if (query.sort) "ORDER BY s.subjectId ASC" else ""
        val sql =
            """
            SELECT * FROM DbSubject s
            LEFT JOIN DbFingerprintSample fp ON s.subjectId = fp.subjectId
            LEFT JOIN DbFaceSample f ON s.subjectId = f.subjectId
            $whereClause
            GROUP BY s.subjectId
             $orderBy
            """.trimIndent()
        return Pair(sql, args)
    }

    private fun buildCountQuery(query: SubjectQuery): Pair<String, List<Any?>> {
        val (whereClause, args) = buildWhereClauseForCount(query)
        // if the query has no face or fingerprint sample format, we can just count the subjects
        val sql = if (query.faceSampleFormat == null && query.fingerprintSampleFormat == null) {
            """
            SELECT count(s.subjectId) FROM DbSubject s            
            $whereClause            
            """.trimIndent()
        } else {
            """
            SELECT count(s.subjectId) FROM DbSubject s
            LEFT JOIN DbSubjectTemplateFormatMap sm ON s.subjectId = sm.subjectId
            $whereClause    
            """.trimIndent()
        }
        return Pair(sql, args)
    }

    private fun buildWhereClauseForCount(query: SubjectQuery): Pair<String, MutableList<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()

        query.projectId?.let {
            whereClauses.add("s.projectId = ?")
            args.add(it)
        }
        query.subjectId?.let {
            whereClauses.add("s.subjectId = ?")
            args.add(it)
        }
        query.subjectIds?.takeIf { it.isNotEmpty() }?.let {
            whereClauses.add("s.subjectId IN (${it.joinToString(",") { "?" }})")
            args.addAll(it)
        }
        query.attendantId?.let {
            whereClauses.add("s.attendantId = ?")
            args.add(it)
        }
        query.moduleId?.let {
            whereClauses.add("s.moduleId = ?")
            args.add(it)
        }

        query.fingerprintSampleFormat?.let {
            whereClauses.add("sm.format = ?")
            args.add(it)
        }
        query.faceSampleFormat?.let {
            whereClauses.add("sm.format = ?")
            args.add(it)
        }
        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        return Pair(whereClause, args)
    }

    private fun buildWhereClause(query: SubjectQuery): Pair<String, MutableList<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()

        query.projectId?.let {
            whereClauses.add("s.projectId = ?")
            args.add(it)
        }
        query.subjectId?.let {
            whereClauses.add("s.subjectId = ?")
            args.add(it)
        }
        query.subjectIds?.takeIf { it.isNotEmpty() }?.let {
            whereClauses.add("s.subjectId IN (${it.joinToString(",") { "?" }})")
            args.addAll(it)
        }
        query.attendantId?.let {
            whereClauses.add("s.attendantId = ?")
            args.add(it)
        }
        query.moduleId?.let {
            whereClauses.add("s.moduleId = ?")
            args.add(it)
        }
        if (query.afterSubjectId != null) {
            whereClauses.add("s.subjectId > ?")
        }
        query.fingerprintSampleFormat?.let {
            whereClauses.add("fp.format = ?")
            args.add(it)
        }
        query.faceSampleFormat?.let {
            whereClauses.add("f.format = ?")
            args.add(it)
        }
        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        return Pair(whereClause, args)
    }

    override suspend fun delete(queries: List<SubjectQuery>) {
        database.withTransaction {
            queries.forEach {
                val (whereClause, args) = buildWhereClause(it)
                val sql = "DELETE FROM DbSubject $whereClause"
                subjectDao.deleteSubjects(SimpleSQLiteQuery(sql, args.toTypedArray()))
            }
        }
    }

    override suspend fun deleteAll() {
        subjectDao.deleteSubjects(SimpleSQLiteQuery("DELETE FROM DbSubject"))
    }

    override suspend fun performActions(
        actions: List<SubjectAction>,
        project: Project,
    ) {
        database.withTransaction {
            actions.forEach { action ->
                when (action) {
                    is SubjectAction.Creation -> createSubject(action.subject, project)
                    is SubjectAction.Update -> updateSubject(action)
                    is SubjectAction.Deletion -> deleteSubject(action.subjectId)
                }
            }
        }
    }

    private suspend fun updateSubject(action: SubjectAction.Update) {
        val dbSubject = subjectDao.getSubject(action.subjectId)
        if (dbSubject != null) {
            // delete face and finger samples
            val referencesToDelete = action.referenceIdsToRemove.toSet() // to make lookup O(1)
            val faceSamplesToDelete = dbSubject.faceSamples.groupBy { it.referenceId in referencesToDelete }
            val fingerprintSamplesToDelete = dbSubject.fingerprintSamples.groupBy { it.referenceId in referencesToDelete }
            faceSamplesToDelete[true]?.forEach { subjectDao.deleteFaceSample(it.uuid) }
            fingerprintSamplesToDelete[true]?.forEach { subjectDao.deleteFingerprintSample(it.uuid) }
            // add face and finger samples
            subjectDao.insertFaceSamples(action.faceSamplesToAdd.map { it.toRoomDb(action.subjectId) })
            subjectDao.insertFingerprintSamples(action.fingerprintSamplesToAdd.map { it.toRoomDb(action.subjectId) })
        } else {
            Simber.i("[SubjectLocalDataSourceImpl] Subject not found for update", tag = REALM_DB)
        }
    }

    private fun TokenizableString.tokenizeIfNecessary(
        tokenKeyType: TokenKeyType,
        project: Project,
    ) = when (this) {
        is TokenizableString.Raw -> tokenizationProcessor.encrypt(
            decrypted = this,
            tokenKeyType = tokenKeyType,
            project = project,
        )

        is TokenizableString.Tokenized -> this
    }

    private suspend fun createSubject(
        subject: SubjectDomain,
        project: Project,
    ) {
        val subjectId = subject.subjectId
        val dbSubject = DbSubject(
            subjectId = subject.subjectId,
            projectId = subject.projectId,
            attendantId = subject.attendantId.tokenizeIfNecessary(TokenKeyType.AttendantId, project).value,
            moduleId = subject.moduleId.tokenizeIfNecessary(TokenKeyType.ModuleId, project).value,
            createdAt = subject.createdAt?.time,
            updatedAt = subject.updatedAt?.time,
        )

        subjectDao.insertSubject(dbSubject)

        val supportedFormats = mutableSetOf<String>()
        // Insert fingerprints
        val dbFingerprints = subject.fingerprintSamples.map {
            supportedFormats.add(it.format)
            it.toRoomDb(subjectId)
        }
        if (dbFingerprints.isNotEmpty()) {
            subjectDao.insertFingerprintSamples(dbFingerprints)
        }

        // Insert face samples
        val dbFaces = subject.faceSamples.map {
            supportedFormats.add(it.format)
            it.toRoomDb(subjectId)
        }
        if (dbFaces.isNotEmpty()) {
            subjectDao.insertFaceSamples(dbFaces)
        }
        supportedFormats.forEach {
            subjectDao.insertSubjectTemplateMapping(
                DbSubjectTemplateFormatMap(
                    subjectId = subjectId,
                    format = it,
                ),
            )
        }
    }

    private suspend fun deleteSubject(subjectId: String) {
        subjectDao.deleteSubject(subjectId)
    }
}

fun log(message: String) {
    Simber.i(message, tag = "roomrecords")
}
