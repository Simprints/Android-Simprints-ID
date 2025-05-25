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
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.TEMPLATE_TABLE_NAME
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ROOM_RECORDS_DB
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.simprints.infra.enrolment.records.repository.domain.models.Subject as DomainSubject

@Singleton
internal class RoomEnrolmentRecordLocalDataSource @Inject constructor(
    private val subjectsDatabaseFactory: SubjectsDatabaseFactory,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
    private val tokenizationProcessor: TokenizationProcessor,
    private val queryBuilder: RoomEnrolmentRecordQueryBuilder,
) : EnrolmentRecordLocalDataSource {
    val database: SubjectsDatabase by lazy {
        subjectsDatabaseFactory.get()
    }
    val subjectDao: SubjectDao by lazy {
        database.subjectDao
    }

    // Public Interface Methods
    override suspend fun load(query: SubjectQuery): List<DomainSubject> = withContext(dispatcherIO) {
        if (query.hasUntokenizedFields == true) {
            Simber.d(
                "[load] Query has untokenized fields, returning empty list as all records are tokenized.",
                tag = ROOM_RECORDS_DB,
            )
            return@withContext emptyList() // All records inserted in this database are tokenized
        }
        subjectDao.loadSubjects(queryBuilder.buildSubjectQuery(query)).groupBy { it.subjectId }.map { it.value.toDomain() }
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = withContext(dispatcherIO) {
        subjectDao.countSubjects(queryBuilder.buildCountQuery(query))
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
        format = query.fingerprintSampleFormat,
        createIdentity = { subjectId, templates ->
            FingerprintIdentity(
                subjectId = subjectId,
                fingerprints = templates.map { sample ->
                    FingerprintSample(
                        fingerIdentifier = IFingerIdentifier.entries[sample.identifier!!],
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
        format = query.faceSampleFormat,
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
                val (whereClause, args) = queryBuilder.buildWhereClause(query)
                val sql = "DELETE FROM $TEMPLATE_TABLE_NAME $whereClause"
                subjectDao.deleteSubjects(SimpleSQLiteQuery(sql, args.toTypedArray()))
            }
        }
    }

    override suspend fun deleteAll() {
        Simber.i("[deleteAll] Deleting all subjects.", tag = ROOM_RECORDS_DB)
        subjectDao.deleteSubjects(SimpleSQLiteQuery("DELETE FROM $TEMPLATE_TABLE_NAME"))
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
        format: String?,
        createIdentity: (subjectId: String, samples: List<DbBiometricTemplate>) -> T,
        onCandidateLoaded: () -> Unit,
    ): List<T> = withContext(dispatcherIO) {
        require(format != null) {
            val errorMsg = "Appropriate sampleFormat is required for loading biometric identities."
            Simber.i("[loadBiometricIdentities] $errorMsg", tag = ROOM_RECORDS_DB)
            errorMsg
        }

        subjectDao
            .loadSamples(queryBuilder.buildBiometricTemplatesQuery(query, range))
            .groupBy { it.subjectId }
            .map {
                onCandidateLoaded()
                createIdentity(it.key, it.value)
            }
    }

    private suspend fun createSubject(
        subject: DomainSubject,
        project: Project,
    ) {
        require(subject.faceSamples.isNotEmpty() || subject.fingerprintSamples.isNotEmpty()) {
            val errorMsg = "Subject should include at least one of the face or fingerprint samples"
            Simber.i(
                "[createSubject] $errorMsg for subjectId: ${subject.subjectId}",
                tag = ROOM_RECORDS_DB,
            )
            errorMsg
        }
        val subjectId = subject.subjectId

        val projectId = subject.projectId
        val attendantId = tokenizationProcessor
            .tokenizeIfNecessary(
                subject.attendantId,
                TokenKeyType.AttendantId,
                project,
            ).value
        val moduleId = tokenizationProcessor
            .tokenizeIfNecessary(
                subject.moduleId,
                TokenKeyType.ModuleId,
                project,
            ).value
        val createdAt = subject.createdAt?.time
        val updatedAt = subject.updatedAt?.time

        subject.fingerprintSamples.takeIf { it.isNotEmpty() }?.let { samples ->
            val dbFingerprints = samples.map {
                it.toRoomDb(subjectId, projectId, attendantId, moduleId, createdAt, updatedAt)
            }
            subjectDao.insertBiometricSamples(dbFingerprints)
        }

        subject.faceSamples.takeIf { it.isNotEmpty() }?.let { samples ->
            val dbFaces = samples.map {
                it.toRoomDb(subjectId, projectId, attendantId, moduleId, createdAt, updatedAt)
            }
            subjectDao.insertBiometricSamples(dbFaces)
        }
    }

    private suspend fun updateSubject(action: SubjectAction.Update) {
        val dbTemplates = subjectDao.getSubject(action.subjectId)

        if (dbTemplates.isNotEmpty()) {
            // delete face and finger samples
            val referencesToDelete = action.referenceIdsToRemove.toSet()
            require(
                referencesToDelete.size != dbTemplates.size ||
                    action.faceSamplesToAdd.isNotEmpty() ||
                    action.fingerprintSamplesToAdd.isNotEmpty(),
            ) {
                val errorMsg = "Cannot delete all samples for subject ${action.subjectId} without adding new ones"
                Simber.i("[updateSubject] $errorMsg", tag = ROOM_RECORDS_DB)
                errorMsg
            }
            val projectId = dbTemplates.first().projectId
            val attendantId = dbTemplates.first().attendantId
            val moduleId = dbTemplates.first().moduleId
            val createdAt = dbTemplates.first().createdAt
            val updatedAt = dbTemplates.first().updatedAt

            dbTemplates.filter { it.referenceId in referencesToDelete }.forEach {
                subjectDao.deleteBiometricSample(it.uuid)
            }

            // add face and finger samples
            val templatesToAdd = action.faceSamplesToAdd.map {
                it.toRoomDb(action.subjectId, projectId, attendantId, moduleId, createdAt, updatedAt)
            } + action.fingerprintSamplesToAdd.map {
                it.toRoomDb(
                    action.subjectId,
                    projectId,
                    attendantId,
                    moduleId,
                    createdAt,
                    updatedAt,
                )
            }

            if (templatesToAdd.isNotEmpty()) {
                subjectDao.insertBiometricSamples(templatesToAdd)
            }
        } else {
            Simber.i(
                "[updateSubject] Subject ${action.subjectId} not found for update",
                tag = ROOM_RECORDS_DB,
            )
        }
    }

    private suspend fun deleteSubject(subjectId: String) {
        Simber.d("[deleteSubject] Deleting subject: $subjectId", tag = ROOM_RECORDS_DB)
        subjectDao.deleteSubject(subjectId)
    }
}
