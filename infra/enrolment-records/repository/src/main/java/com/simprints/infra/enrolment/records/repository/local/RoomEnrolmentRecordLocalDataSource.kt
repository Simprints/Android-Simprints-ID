package com.simprints.infra.enrolment.records.repository.local

import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.simprints.core.DispatcherIO
import com.simprints.infra.config.store.models.Project
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
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue
import com.simprints.infra.enrolment.records.repository.domain.models.Subject as SubjectDomain

@Singleton
internal class RoomEnrolmentRecordLocalDataSource @Inject constructor(
    private val subjectsDatabaseFactory: SubjectsDatabaseFactory,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) : EnrolmentRecordLocalDataSource {
    val database: SubjectsDatabase by lazy { subjectsDatabaseFactory.get() }
    val subjectDao: SubjectDao by lazy { subjectsDatabaseFactory.get().subjectDao }

    override suspend fun load(query: SubjectQuery): List<SubjectDomain> = withContext(dispatcherIO) {
        val sqlQuery = buildSubjectQuery(query)
        subjectDao.loadSubjects(SimpleSQLiteQuery(sqlQuery.first, sqlQuery.second.toTypedArray())).map { it.toDomain() }
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ): List<FingerprintIdentity> = withContext(dispatcherIO) {
        subjectDao
            .getSubjectsWithFingerprintSamples(
                query.projectId,
                query.subjectId,
                query.subjectIds,
                query.attendantId?.value,
                query.moduleId?.value,
                query.fingerprintSampleFormat,
                range.first,
                range.last,
            ).map {
                onCandidateLoaded()
                FingerprintIdentity(
                    subjectId = it.key,
                    fingerprints = it.value.map { sample -> sample.toDomain() },
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
        val result = measureTimedValue {
            subjectDao
                .getSubjectsWithFaceSamples(
                    query.projectId,
                    query.subjectId,
                    query.subjectIds,
                    query.attendantId?.value,
                    query.moduleId?.value,
                    query.faceSampleFormat,
                    range.first,
                    range.last,
                ).map {
                    onCandidateLoaded()
                    FaceIdentity(
                        subjectId = it.key,
                        faces = it.value.map { sample -> sample.toDomain() },
                    )
                }
        }
        log("loadFaceIdentities ${result.value.size} in  ${result.duration.inWholeMilliseconds} ms")
        return@withContext result.value
    }

    private fun buildSubjectQuery(query: SubjectQuery): Pair<String, List<Any?>> {
        val (whereClause, args) = buildWhereClause(query)
        val orderBy = if (query.sort) "ORDER BY s.subjectId ASC" else ""
        val sql =
            """
            SELECT * FROM DbSubject s
            LEFT JOIN DbFingerprintSample fingerprint ON s.subjectId = fingerprint.subjectId
            LEFT JOIN DbFaceSample face ON s.subjectId = face.subjectId
            $whereClause
            $orderBy
            """.trimIndent()
        return Pair(sql, args)
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
        if (query.hasUntokenizedFields == true) {
            whereClauses.add("(s.isAttendantIdTokenized = 0 OR s.isModuleIdTokenized = 0)")
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

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = withContext(dispatcherIO) {
        var result = 0
        val timeTaken = measureTimeMillis {
            result = subjectDao.countSubjects()
        }
        log("count $result : $timeTaken ms")
        result
    }

    override suspend fun performActions(
        actions: List<SubjectAction>,
        project: Project,
    ) {
        val timeTaken = measureTimeMillis {
            database.withTransaction {
                actions.forEach { action ->
                    when (action) {
                        is SubjectAction.Creation -> createSubject(action.subject)
                        is SubjectAction.Update -> Unit
                        is SubjectAction.Deletion -> deleteSubject(action.subjectId)
                    }
                }
            }
        }
        log("performActions ${actions.size}  in : $timeTaken ms")
    }

    private suspend fun createSubject(subject: SubjectDomain) {
        val subjectId = subject.subjectId
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
        val dbFingerprints = subject.fingerprintSamples.map { it.toRoomDb(subjectId) }
        if (dbFingerprints.isNotEmpty()) {
            subjectDao.insertFingerprintSamples(dbFingerprints)
        }

        // Insert face samples
        val dbFaces = subject.faceSamples.map { it.toRoomDb(subjectId) }
        if (dbFaces.isNotEmpty()) {
            subjectDao.insertFaceSamples(dbFaces)
        }
    }

    private suspend fun deleteSubject(subjectId: String) {
        subjectDao.deleteSubject(subjectId)
    }
}

fun log(message: String) {
    Simber.i(message, tag = "roomrecords")
}
