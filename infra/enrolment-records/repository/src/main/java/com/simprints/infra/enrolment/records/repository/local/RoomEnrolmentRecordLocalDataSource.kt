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
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ROOM_RECORDS_DB
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.simprints.infra.enrolment.records.repository.domain.models.Subject as DomainSubject

/**
 * Local data source for enrolment records using Room.
 */
@Singleton
internal class RoomEnrolmentRecordLocalDataSource @Inject constructor(
    private val subjectsDatabaseFactory: SubjectsDatabaseFactory,
    private val tokenizationProcessor: TokenizationProcessor,
    private val queryBuilder: RoomEnrolmentRecordQueryBuilder,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) : EnrolmentRecordLocalDataSource {
    companion object {
        // only one concurrent operation is allowed as we use the last seen subject ID to load biometric identities
        private const val PARALLELISM = 1
    }

    private val database: SubjectsDatabase by lazy { subjectsDatabaseFactory.get() }
    private val subjectDao: SubjectDao by lazy { database.subjectDao }

    /**
     * Loads subjects matching the given query.
     * Don't use this method if the query contains format fields (faceSampleFormat or fingerprintSampleFormat).
     * instead, use loadFaceIdentities or loadFingerprintIdentities methods.
     */
    override suspend fun load(query: SubjectQuery): List<DomainSubject> = withContext(dispatcherIO) {
        if (query.hasUntokenizedFields == true) {
            Simber.d(
                "[load] Query has untokenized fields, returning empty list as all records are tokenized.",
                tag = ROOM_RECORDS_DB,
            )
            return@withContext emptyList()
        }
        subjectDao.loadSubjects(queryBuilder.buildSubjectQuery(query)).map { it.toDomain() }
    }

    /**
     * Counts subjects matching the given query.
     */
    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = withContext(dispatcherIO) {
        subjectDao.countSubjects(queryBuilder.buildCountQuery(query))
    }

    /**
     * Loads face identities in paged ranges.
     */
    override fun loadFaceIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: () -> Unit,
    ): ReceiveChannel<List<FaceIdentity>> = loadBiometricIdentitiesPaged(
        query = query,
        ranges = ranges,
        format = requireNotNull(query.faceSampleFormat) { "faceSampleFormat required" },
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
        scope = scope,
    )

    /**
     * Loads fingerprint identities in paged ranges.
     */
    override fun loadFingerprintIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: () -> Unit,
    ): ReceiveChannel<List<FingerprintIdentity>> = loadBiometricIdentitiesPaged(
        query = query,
        ranges = ranges,
        format = requireNotNull(query.fingerprintSampleFormat) { "fingerprintSampleFormat required" },
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
        scope = scope,
    )

    private fun <T> loadBiometricIdentitiesPaged(
        query: SubjectQuery,
        ranges: List<IntRange>,
        format: String,
        createIdentity: (String, List<DbBiometricTemplate>) -> T,
        onCandidateLoaded: () -> Unit,
        scope: CoroutineScope,
    ): ReceiveChannel<List<T>> {
        var lastSeenSubjectId: String? = null
        var lastOffset = 0
        return loadIdentitiesConcurrently(
            ranges = ranges,
            dispatcher = dispatcherIO,
            parallelism = PARALLELISM,
            scope = scope,
        ) { range ->
            require(lastOffset == range.first) {
                "[loadBiometricIdentitiesPaged] The range start must match the last seen sample count. " +
                    "Expected: $lastOffset, Actual: ${range.first}"
            }
            val identities = loadBiometricIdentities(
                query = query,
                pageSize = range.last - range.first,
                lastSeenSubjectId = lastSeenSubjectId,
                format = format,
                createIdentity = createIdentity,
                onCandidateLoaded = onCandidateLoaded,
            )
            lastSeenSubjectId = identities.lastOrNull()?.let {
                (it as? FaceIdentity)?.subjectId ?: (it as? FingerprintIdentity)?.subjectId
            }
            lastOffset = range.last
            identities
        }
    }

    private suspend fun <T> loadBiometricIdentities(
        query: SubjectQuery,
        pageSize: Int,
        format: String?,
        lastSeenSubjectId: String?,
        createIdentity: (subjectId: String, samples: List<DbBiometricTemplate>) -> T,
        onCandidateLoaded: () -> Unit,
    ): List<T> = withContext(dispatcherIO) {
        requireNotNull(format) { "Appropriate sampleFormat is required for loading biometric identities." }
        subjectDao
            .loadSamples(queryBuilder.buildBiometricTemplatesQuery(query, pageSize, lastSeenSubjectId))
            .map { (subjectId, templates) ->
                onCandidateLoaded()
                createIdentity(subjectId, templates)
            }
    }

    override suspend fun delete(queries: List<SubjectQuery>) {
        Simber.i("[delete] Deleting subjects with queries: $queries", tag = ROOM_RECORDS_DB)
        database.withTransaction {
            queries.forEach { query ->
                require(query.faceSampleFormat == null && query.fingerprintSampleFormat == null) {
                    val errorMsg = "faceSampleFormat and fingerprintSampleFormat are not supported for deletion"
                    Simber.i("[delete] $errorMsg", tag = ROOM_RECORDS_DB)
                    errorMsg
                }
                val (whereClause, args) = queryBuilder.buildWhereClause(
                    query,
                    subjectAlias = "",
                    templateAlias = "",
                )
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
        val dbSubject = DbSubject(
            subjectId = subject.subjectId,
            projectId = subject.projectId,
            attendantId = tokenizationProcessor
                .tokenizeIfNecessary(
                    subject.attendantId,
                    TokenKeyType.AttendantId,
                    project,
                ).value,
            moduleId = tokenizationProcessor
                .tokenizeIfNecessary(
                    subject.moduleId,
                    TokenKeyType.ModuleId,
                    project,
                ).value,
            createdAt = subject.createdAt?.time,
            updatedAt = subject.updatedAt?.time,
        )
        subjectDao.insertSubject(dbSubject)
        subject.fingerprintSamples.takeIf { it.isNotEmpty() }?.let { samples ->
            val dbFingerprints = samples.map { it.toRoomDb(subject.subjectId) }
            subjectDao.insertBiometricSamples(dbFingerprints)
        }
        subject.faceSamples.takeIf { it.isNotEmpty() }?.let { samples ->
            val dbFaces = samples.map { it.toRoomDb(subject.subjectId) }
            subjectDao.insertBiometricSamples(dbFaces)
        }
    }

    private suspend fun updateSubject(action: SubjectAction.Update) {
        val dbSubject = subjectDao.getSubject(action.subjectId)
        if (dbSubject != null) {
            val referencesToDelete = action.referenceIdsToRemove.toSet()
            require(
                referencesToDelete.size != dbSubject.biometricTemplates.size ||
                    action.faceSamplesToAdd.isNotEmpty() ||
                    action.fingerprintSamplesToAdd.isNotEmpty(),
            ) {
                val errorMsg = "Cannot delete all samples for subject ${action.subjectId} without adding new ones"
                Simber.i("[updateSubject] $errorMsg", tag = ROOM_RECORDS_DB)
                errorMsg
            }
            dbSubject.biometricTemplates.filter { it.referenceId in referencesToDelete }.forEach {
                subjectDao.deleteBiometricSample(it.uuid)
            }
            val templatesToAdd =
                action.faceSamplesToAdd.map { it.toRoomDb(action.subjectId) } +
                    action.fingerprintSamplesToAdd.map { it.toRoomDb(action.subjectId) }
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
