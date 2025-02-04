package com.simprints.infra.enrolment.records.store.local

import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.simprints.core.DispatcherIO
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.store.local.models.DbFaceSample
import com.simprints.infra.enrolment.records.store.local.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.store.local.models.DbSubject
import com.simprints.infra.enrolment.records.store.local.models.fromDbToDomain
import com.simprints.infra.enrolment.records.store.local.models.toDomain
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis
import com.simprints.infra.enrolment.records.store.domain.models.Subject as SubjectDomain

@Singleton
internal class EnrolmentRecordLocalDataSourceImpl @Inject constructor(
    private val subjectsDatabaseFactory: SubjectsDatabaseFactory,
    @DispatcherIO private val dispatcherIO: kotlinx.coroutines.CoroutineDispatcher,
) : EnrolmentRecordLocalDataSource {
    companion object {
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "attendantId"
        const val SUBJECT_ID_FIELD = "subjectId"
        const val MODULE_ID_FIELD = "moduleId"
        const val IS_ATTENDANT_ID_TOKENIZED_FIELD = "isAttendantIdTokenized"
        const val IS_MODULE_ID_TOKENIZED_FIELD = "isModuleIdTokenized"
        const val FINGERPRINT_SAMPLES_FIELD = "fingerprintSamples"
        const val FACE_SAMPLES_FIELD = "faceSamples"
        const val FORMAT_FIELD = "format"
    }

    val database: SubjectsDatabase by lazy { subjectsDatabaseFactory.build() }
    val subjectDao: SubjectDao by lazy { database.subjectDao }

    override suspend fun load(query: SubjectQuery): List<SubjectDomain> = withContext(dispatcherIO) {
        val sqlQuery = buildQuery(query)
        Simber.d("Query: ${sqlQuery.first}, Params: ${sqlQuery.second}", tag = "SQLQUERY")
        var result: List<SubjectDomain> = emptyList()
        val timeTaken = measureTimeMillis {
            result = subjectDao.loadSubjects(SimpleSQLiteQuery(sqlQuery.first, sqlQuery.second.toTypedArray())).map {
                it.toDomain()
            }
        }
        log("Time taken to load ${result.size} : $timeTaken ms")
        result
    }

    private fun buildQuery(query: SubjectQuery): Pair<String, List<Any?>> {
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
            whereClauses.add("fs.format = ?")
            args.add(it)
        }
        query.faceSampleFormat?.let {
            whereClauses.add("fs2.format = ?")
            args.add(it)
        }
        if (query.hasUntokenizedFields == true) {
            whereClauses.add("(s.isAttendantIdTokenized = 0 OR s.isModuleIdTokenized = 0)")
        }

        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""

        val orderBy = if (query.sort) "ORDER BY s.subjectId ASC" else ""

        val sql =
            """
            SELECT * FROM subjects s
            LEFT JOIN fingerprint_samples fs ON s.subjectId = fs.subjectId
            LEFT JOIN face_samples fs2 ON s.subjectId = fs2.subjectId
            $whereClause
            $orderBy
            """.trimIndent()

        return Pair(sql, args)
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
    ): List<FingerprintIdentity> {
        val sqlQuery = buildFingerprintQuery(query, range)
        return subjectDao.loadSubjects(SimpleSQLiteQuery(sqlQuery.first, sqlQuery.second.toTypedArray())).map {
            FingerprintIdentity(
                subjectId = it.subject.subjectId,
                fingerprints = it.fingerprintSamples.map { sample ->
                    sample.fromDbToDomain()
                },
            )
        }
    }

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
    ): List<FaceIdentity> = withContext(dispatcherIO) {
        val sqlQuery = buildFaceQuery(query, range)
        var result: List<FaceIdentity> = emptyList()
        val timeTaken = measureTimeMillis {
            result = subjectDao.loadSubjects(SimpleSQLiteQuery(sqlQuery.first, sqlQuery.second.toTypedArray())).map {
                FaceIdentity(
                    subjectId = it.subject.subjectId,
                    faces = it.faceSamples.map { sample ->
                        sample.fromDbToDomain()
                    },
                )
            }
        }
        log("load ${result.size} : $timeTaken ms")
        result
    }

    override suspend fun delete(queries: List<SubjectQuery>) {
    }

    override suspend fun deleteAll() {
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = withContext(dispatcherIO) {
        val sqlQuery = buildCountQuery(query)
        var result = 0
        val timeTaken = measureTimeMillis {
            result = subjectDao.count(SimpleSQLiteQuery(sqlQuery.first, sqlQuery.second.toTypedArray()))
        }
        log("count $result : $timeTaken ms")
        result
    }

    private fun buildCountQuery(query: SubjectQuery): Pair<String, List<Any?>> {
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

        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""

        val sql =
            """
            SELECT COUNT(*) FROM subjects s
            $whereClause
            """.trimIndent()

        return Pair(sql, args)
    }

    private fun buildFingerprintQuery(
        query: SubjectQuery,
        range: IntRange,
    ): Pair<String, List<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()

        query.projectId?.let {
            whereClauses.add("s.projectId = ?")
            args.add(it)
        }
        query.fingerprintSampleFormat?.let {
            whereClauses.add("fs.format = ?")
            args.add(it)
        }

        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        val limitClause = "LIMIT ? OFFSET ?"
        args.add(range.last - range.first + 1)
        args.add(range.first)

        val sql =
            """
            SELECT s.subjectId, fs.id, fs.template, fs.format
            FROM subjects s
            LEFT JOIN fingerprint_samples fs ON s.subjectId = fs.subjectId
            $whereClause
            $limitClause
            """.trimIndent()

        return Pair(sql, args)
    }

    private fun buildFaceQuery(
        query: SubjectQuery,
        range: IntRange,
    ): Pair<String, List<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()

        query.projectId?.let {
            whereClauses.add("s.projectId = ?")
            args.add(it)
        }
        query.faceSampleFormat?.let {
            whereClauses.add("fs.format = ?")
            args.add(it)
        }

        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        val limitClause = "LIMIT ? OFFSET ?"
        args.add(range.last - range.first + 1)
        args.add(range.first)

        val sql =
            """
            SELECT s.subjectId, fs.id, fs.template, fs.format
            FROM subjects s
            LEFT JOIN face_samples fs ON s.subjectId = fs.subjectId
            $whereClause
            $limitClause
            """.trimIndent()

        return Pair(sql, args)
    }

    override suspend fun performActions(actions: List<SubjectAction>) {
        val timeTaken = measureTimeMillis {
            database.withTransaction {
                actions.forEach { action ->
                    when (action) {
                        is SubjectAction.Creation -> createSubject(action.subject)
                        is SubjectAction.Deletion -> deleteSubject(action.subjectId)
                    }
                }
            }
        }
        log("performActions ${actions.size}  in : $timeTaken ms")
    }

    private suspend fun createSubject(subject: SubjectDomain) {
        val dbSubject = DbSubject(
            subjectId = subject.subjectId,
            projectId = subject.projectId,
            attendantId = subject.attendantId.value,
            moduleId = subject.moduleId.value,
            createdAt = subject.createdAt?.time,
            updatedAt = subject.updatedAt?.time,
            isAttendantIdTokenized = false,
            isModuleIdTokenized = false,
        )

        subjectDao.insertSubject(dbSubject)

        // Insert fingerprints
        val dbFingerprints = subject.fingerprintSamples.map {
            DbFingerprintSample(
                id = UUID.randomUUID().toString(),
                subjectId = subject.subjectId,
                template = it.template,
                format = it.format,
                templateQualityScore = it.templateQualityScore,
                fingerIdentifier = it.fingerIdentifier.ordinal,
            )
        }
        subjectDao.insertAllFingerprintSample(dbFingerprints)

        // Insert face samples
        val dbFaces = subject.faceSamples.map {
            DbFaceSample(
                id = UUID.randomUUID().toString(),
                subjectId = subject.subjectId,
                template = it.template,
                format = "RANK_ONE_3_1",
            )
        }
        subjectDao.insertAllFaceSample(dbFaces)
        // log every subject creation
        //  Simber.i("Subject created: $subject", tag = "SQLQUERY")
    }

    private suspend fun deleteSubject(subjectId: String) {
//        fingerprintDao.deleteBySubjectId(subjectId)
//        faceDao.deleteBySubjectId(subjectId)
//        subjectDao.deleteById(subjectId)
    }

//
//
//        if (query.fingerprintSampleFormat != null) {
//            realmQuery = realmQuery.query(
//                "ANY $FINGERPRINT_SAMPLES_FIELD.$FORMAT_FIELD == $0",
//                query.fingerprintSampleFormat,
//            )
//        }
//        if (query.faceSampleFormat != null) {
//            realmQuery = realmQuery.query(
//                "ANY $FACE_SAMPLES_FIELD.$FORMAT_FIELD == $0",
//                query.faceSampleFormat,
//            )
//        }
//        if (query.afterSubjectId != null) {
//            realmQuery = realmQuery.query(
//                "$SUBJECT_ID_FIELD >= $0",
//                RealmUUID.from(query.afterSubjectId),
//            )
//        }
}

fun log(message: String) {
    Simber.i(message, tag = "roomrecords")
}
