package com.simprints.infra.enrolment.records.repository.local

import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
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
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import com.simprints.infra.enrolment.records.room.store.models.DbSubjectTemplateFormatMap
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ROOM_RECORDS_DB
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
    val database: SubjectsDatabase by lazy {
        subjectsDatabaseFactory.get()
    }
    val subjectDao: SubjectDao by lazy {
        database.subjectDao
    }

    // Public Interface Methods
    override suspend fun load(query: SubjectQuery): List<SubjectDomain> = withContext(dispatcherIO) {
        if (query.hasUntokenizedFields == true) {
            Simber.d("[load] Query has untokenized fields, returning empty list as all records are tokenized.", tag = ROOM_RECORDS_DB)
            return@withContext emptyList() // All records inserted in this database are tokenized
        }
        val sqlQuery = buildSubjectQuery(query)
        subjectDao.loadSubjects(SimpleSQLiteQuery(sqlQuery.first, sqlQuery.second.toTypedArray())).map { it.toDomain() }
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = withContext(dispatcherIO) {
        val (sql, queryArgs) = buildCountQuery(query)
        subjectDao.countSubjects(SimpleSQLiteQuery(sql, queryArgs.toTypedArray()))
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ): List<FingerprintIdentity> = loadBiometricIdentities(
        query = query,
        range = range,
        getSampleFormat = { it.fingerprintSampleFormat },
        createIdentity = { subjectId, templates ->
            FingerprintIdentity(
                subjectId = subjectId,
                fingerprints = templates.map { sample ->
                    FingerprintSample(
                        fingerIdentifier = IFingerIdentifier.entries[sample.fingerIdentifier!!],
                        template = sample.templateData,
                        id = sample.uuid,
                        format = sample.format,
                        referenceId = sample.referenceId,
                    )
                },
            )
        },
        onCandidateLoaded = onCandidateLoaded,
    )

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ): List<FaceIdentity> = loadBiometricIdentities(
        query = query,
        range = range,
        getSampleFormat = { it.faceSampleFormat },
        createIdentity = { subjectId, templates ->
            FaceIdentity(
                subjectId = subjectId,
                faces = templates.map { sample ->
                    FaceSample(
                        template = sample.templateData,
                        id = sample.uuid,
                        format = sample.format,
                        referenceId = sample.referenceId,
                    )
                },
            )
        },
        onCandidateLoaded = onCandidateLoaded,
    )

    override suspend fun delete(queries: List<SubjectQuery>) {
        Simber.i("[delete] Deleting subjects with queries: $queries", tag = ROOM_RECORDS_DB)
        database.withTransaction {
            queries.forEach { query ->
                require(query.faceSampleFormat == null && query.fingerprintSampleFormat == null) {
                    val errorMsg = "faceSampleFormat and fingerprintSampleFormat are not supported for deletion"
                    Simber.i("[delete] $errorMsg", tag = ROOM_RECORDS_DB)
                    errorMsg
                }
                val (whereClause, args) = buildWhereClauseForDelete(query)
                val sql = "DELETE FROM DbSubject $whereClause"
                subjectDao.deleteSubjects(SimpleSQLiteQuery(sql, args.toTypedArray()))
            }
        }
    }

    override suspend fun deleteAll() {
        Simber.i("[deleteAll] Deleting all subjects.", tag = ROOM_RECORDS_DB)
        subjectDao.deleteSubjects(SimpleSQLiteQuery("DELETE FROM DbSubject"))
    }

    override suspend fun performActions(
        actions: List<SubjectAction>,
        project: Project,
    ) {
        database.withTransaction {
            actions.forEach { action ->
                Simber.d("[performActions] Performing action: $action", tag = ROOM_RECORDS_DB)
                when (action) {
                    is SubjectAction.Creation -> createSubject(action.subject, project)
                    is SubjectAction.Update -> updateSubject(action)
                    is SubjectAction.Deletion -> deleteSubject(action.subjectId)
                }
            }
        }
    }

    // Private Helper Methods
    private suspend fun <T> loadBiometricIdentities(
        query: SubjectQuery,
        range: IntRange,
        getSampleFormat: (SubjectQuery) -> String?,
        createIdentity: (subjectId: String, samples: List<DbBiometricTemplate>) -> T,
        onCandidateLoaded: () -> Unit,
    ): List<T> = withContext(dispatcherIO) {
        val sampleFormat = getSampleFormat(query)
        require(sampleFormat != null) {
            val errorMsg = "Appropriate sampleFormat is required for loading biometric identities."
            Simber.i("[loadBiometricIdentities] $errorMsg", tag = ROOM_RECORDS_DB)
            errorMsg
        }

        val sqlQuery = buildBiometricTemplatesQuery(query, range)
        subjectDao
            .loadSamples(SimpleSQLiteQuery(sqlQuery.first, sqlQuery.second.toTypedArray()))
            .map { (subjectId, templates) ->
                onCandidateLoaded()
                createIdentity(subjectId, templates)
            }
    }

    private suspend fun createSubject(
        subject: SubjectDomain,
        project: Project,
    ) {
        require(subject.faceSamples.isNotEmpty() || subject.fingerprintSamples.isNotEmpty()) {
            val errorMsg = "Subject should include at least one of the face or fingerprint samples"
            Simber.i("[createSubject] $errorMsg for subjectId: ${subject.subjectId}", tag = ROOM_RECORDS_DB)
            errorMsg
        }
        val subjectId = subject.subjectId
        val dbSubject = DbSubject(
            subjectId = subject.subjectId,
            projectId = subject.projectId,
            attendantId = tokenizationProcessor.tokenizeIfNecessary(subject.attendantId, TokenKeyType.AttendantId, project).value,
            moduleId = tokenizationProcessor.tokenizeIfNecessary(subject.moduleId, TokenKeyType.ModuleId, project).value,
            createdAt = subject.createdAt?.time,
            updatedAt = subject.updatedAt?.time,
        )

        subjectDao.insertSubject(dbSubject)

        val supportedFormats = mutableSetOf<String>()

        subject.fingerprintSamples.takeIf { it.isNotEmpty() }?.let { samples ->
            val dbFingerprints = samples.map {
                supportedFormats.add(it.format)
                it.toRoomDb(subjectId)
            }
            subjectDao.insertBiometricSamples(dbFingerprints)
        }

        subject.faceSamples.takeIf { it.isNotEmpty() }?.let { samples ->
            val dbFaces = samples.map {
                supportedFormats.add(it.format)
                it.toRoomDb(subjectId)
            }
            subjectDao.insertBiometricSamples(dbFaces)
        }
        supportedFormats.forEach { format ->
            subjectDao.insertSubjectTemplateMapping(
                DbSubjectTemplateFormatMap(
                    subjectId = subjectId,
                    format = format,
                ),
            )
        }
    }

    private suspend fun updateSubject(action: SubjectAction.Update) {
        val dbSubject = subjectDao.getSubject(action.subjectId)

        if (dbSubject != null) {
            // delete face and finger samples
            val referencesToDelete = action.referenceIdsToRemove.toSet()
            require(
                referencesToDelete.size != dbSubject.biometricSamples.size ||
                    action.faceSamplesToAdd.isNotEmpty() ||
                    action.fingerprintSamplesToAdd.isNotEmpty(),
            ) {
                val errorMsg = "Cannot delete all samples for subject ${action.subjectId} without adding new ones"
                Simber.i("[updateSubject] $errorMsg", tag = ROOM_RECORDS_DB)
                errorMsg
            }
            dbSubject.biometricSamples
                .filter { it.referenceId in referencesToDelete }
                .forEach {
                    subjectDao.deleteBiometricSample(it.uuid)
                }

            // add face and finger samples
            if (action.faceSamplesToAdd.isNotEmpty()) {
                subjectDao.insertBiometricSamples(action.faceSamplesToAdd.map { it.toRoomDb(action.subjectId) })
            }
            if (action.fingerprintSamplesToAdd.isNotEmpty()) {
                subjectDao.insertBiometricSamples(action.fingerprintSamplesToAdd.map { it.toRoomDb(action.subjectId) })
            }
        } else {
            Simber.i("[updateSubject] Subject ${action.subjectId} not found for update", tag = ROOM_RECORDS_DB)
        }
    }

    private suspend fun deleteSubject(subjectId: String) {
        Simber.d("[deleteSubject] Deleting subject: $subjectId", tag = ROOM_RECORDS_DB)
        subjectDao.deleteSubject(subjectId)
    }

    // SQL Query Builder Methods
    private fun buildSubjectQuery(query: SubjectQuery): Pair<String, List<Any?>> {
        val (whereClause, args) = buildWhereClause(query)
        val orderBy = if (query.sort) "ORDER BY s.subjectId ASC" else ""
        val sql =
            """
            SELECT * FROM DbSubject s
            LEFT JOIN DbBiometricTemplate b ON s.subjectId = b.subjectId
            $whereClause
            GROUP BY s.subjectId
             $orderBy
            """.trimIndent()
        return Pair(sql, args)
    }

    private fun buildCountQuery(query: SubjectQuery): Pair<String, List<Any?>> {
        val args = mutableListOf<Any?>()
        val whereClauses = mutableListOf<String>()
        val sqlBuilder = StringBuilder()

        val specificFormat = query.fingerprintSampleFormat ?: query.faceSampleFormat

        if (specificFormat != null) {
            sqlBuilder.append("SELECT COUNT(DISTINCT s.subjectId) FROM DbSubject s ")
            sqlBuilder.append("INNER JOIN DbSubjectTemplateFormatMap m ON s.subjectId = m.subjectId")
            appendCommonSubjectConditions(query, whereClauses, args, "s.")
            whereClauses.add("m.format = ?")
            args.add(specificFormat)
        } else {
            sqlBuilder.append("SELECT COUNT(s.subjectId) FROM DbSubject s")
            appendCommonSubjectConditions(query, whereClauses, args, "s.")
        }

        if (whereClauses.isNotEmpty()) {
            sqlBuilder.append(" WHERE ").append(whereClauses.joinToString(" AND "))
        }
        val resultQuery = sqlBuilder.toString()
        return Pair(resultQuery, args)
    }

    private fun buildBiometricTemplatesQuery(
        query: SubjectQuery,
        range: IntRange,
    ): Pair<String, List<Any?>> {
        val (whereClause, args) = buildBiometricTemplatesWhereClause(query)
        val format = query.fingerprintSampleFormat ?: query.faceSampleFormat
            ?: throw IllegalArgumentException("Either fingerprintSampleFormat or faceSampleFormat must be provided").also {
                Simber.i(
                    "[buildBiometricTemplatesQuery] Missing fingerprintSampleFormat or faceSampleFormat in query: $query",
                    tag = ROOM_RECORDS_DB,
                )
            }
        val sql =
            """
        SELECT b.*
        FROM DbBiometricTemplate b
        JOIN (
            SELECT s.subjectId
            FROM DbSubject s
            INNER JOIN DbSubjectTemplateFormatMap m ON s.subjectId = m.subjectId
            where m.format = "$format"
            $whereClause                 
            ORDER BY s.createdAt
            LIMIT ${range.last - range.first} OFFSET ${range.first}
        ) AS filterSubjects
        ON b.subjectId = filterSubjects.subjectId AND b.format = "$format"
        """
        return Pair(sql, args)
    }

    private fun buildWhereClause(query: SubjectQuery): Pair<String, MutableList<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()

        appendCommonSubjectConditions(query, whereClauses, args, "s.") // Use "s." alias

        query.fingerprintSampleFormat?.let {
            whereClauses.add("b.format = ?")
            args.add(it)
        }
        query.faceSampleFormat?.let {
            whereClauses.add("b.format = ?")
            args.add(it)
        }

        val whereClauseResult = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        return Pair(whereClauseResult, args)
    }

    private fun buildBiometricTemplatesWhereClause(query: SubjectQuery): Pair<String, MutableList<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()
        appendCommonSubjectConditions(query, whereClauses, args, "s.")
        val whereClauseResult = if (whereClauses.isNotEmpty()) "AND ${whereClauses.joinToString(" AND ")}" else ""
        return Pair(whereClauseResult, args)
    }

    private fun buildWhereClauseForDelete(query: SubjectQuery): Pair<String, MutableList<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()
        appendCommonSubjectConditions(query, whereClauses, args, "")
        val whereClauseResult = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        return Pair(whereClauseResult, args)
    }

    private fun appendCommonSubjectConditions(
        query: SubjectQuery,
        clauses: MutableList<String>,
        args: MutableList<Any?>,
        aliasPrefix: String,
    ) {
        query.projectId?.let {
            clauses.add("${aliasPrefix}projectId = ?")
            args.add(it)
        }
        query.subjectId?.let {
            clauses.add("${aliasPrefix}subjectId = ?")
            args.add(it)
        }
        query.subjectIds?.takeIf { it.isNotEmpty() }?.let {
            clauses.add("${aliasPrefix}subjectId IN (${it.joinToString(",") { "?" }})")
            args.addAll(it)
        }
        query.attendantId?.let {
            clauses.add("${aliasPrefix}attendantId = ?")
            args.add(it.value)
        }
        query.moduleId?.let {
            clauses.add("${aliasPrefix}moduleId = ?")
            args.add(it.value)
        }
        query.afterSubjectId?.let {
            clauses.add("${aliasPrefix}subjectId > ?")
            args.add(it)
        }
    }
}
