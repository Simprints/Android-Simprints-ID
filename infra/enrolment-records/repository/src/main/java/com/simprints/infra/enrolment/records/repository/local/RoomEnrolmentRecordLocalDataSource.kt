package com.simprints.infra.enrolment.records.repository.local

import androidx.room.withTransaction
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.CandidateRecordBatch
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.enrolment.records.repository.local.models.toBiometricReferences
import com.simprints.infra.enrolment.records.repository.local.models.toDomain
import com.simprints.infra.enrolment.records.repository.local.models.toRoomDbCredentials
import com.simprints.infra.enrolment.records.repository.local.models.toRoomDbTemplate
import com.simprints.infra.enrolment.records.room.store.BuildConfig.DB_ENCRYPTION
import com.simprints.infra.enrolment.records.room.store.SubjectDao
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabaseFactory
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ROOM_RECORDS_DB
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.simprints.infra.enrolment.records.repository.domain.models.Subject as DomainSubject

/**
 * Local data source for enrolment records using Room.
 */
@Singleton
internal class RoomEnrolmentRecordLocalDataSource @Inject constructor(
    private val timeHelper: TimeHelper,
    private val subjectsDatabaseFactory: SubjectsDatabaseFactory,
    private val tokenizationProcessor: TokenizationProcessor,
    private val queryBuilder: RoomEnrolmentRecordQueryBuilder,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) : EnrolmentRecordLocalDataSource {
    companion object {
        // Although batches are processed sequentially, we use a small channel capacity to prevent blocking and reduce the risk of out-of-memory errors.
        private const val CHANNEL_CAPACITY = 4
    }

    private val database: SubjectsDatabase by lazy { subjectsDatabaseFactory.get() }
    private val subjectDao: SubjectDao by lazy { database.subjectDao }

    /**
     * Loads subjects matching the given query.
     * Don't use this method if the query contains format fields (faceSampleFormat or fingerprintSampleFormat).
     * instead, use loadFaceIdentities or loadFingerprintIdentities methods.
     */
    override suspend fun load(query: EnrolmentRecordQuery): List<DomainSubject> = withContext(dispatcherIO) {
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
        query: EnrolmentRecordQuery,
        dataSource: BiometricDataSource,
    ): Int = withContext(dispatcherIO) {
        subjectDao.countSubjects(queryBuilder.buildCountQuery(query))
    }

    /**
     * Loads identities in paged ranges.
     */
    override suspend fun loadCandidateRecords(
        query: EnrolmentRecordQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ): ReceiveChannel<CandidateRecordBatch> = loadBiometricIdentitiesPaged(
        query = query,
        ranges = ranges,
        format = requireNotNull(query.format) { "format required" },
        createCandidateRecord = { subjectId, templates ->
            CandidateRecord(
                subjectId = subjectId,
                references = templates.toBiometricReferences(),
            )
        },
        onCandidateLoaded = onCandidateLoaded,
        scope = scope,
    )

    private fun loadBiometricIdentitiesPaged(
        query: EnrolmentRecordQuery,
        ranges: List<IntRange>,
        format: String,
        createCandidateRecord: (String, List<DbBiometricTemplate>) -> CandidateRecord,
        onCandidateLoaded: suspend () -> Unit,
        scope: CoroutineScope,
    ): ReceiveChannel<CandidateRecordBatch> {
        var afterSubjectId: String? = null
        var lastOffset = 0
        val channel = Channel<CandidateRecordBatch>(CHANNEL_CAPACITY)
        scope.launch(dispatcherIO) {
            ranges
                .forEach { range ->
                    require(lastOffset == range.first) {
                        "[loadBiometricIdentitiesPaged] The range start must match the last seen sample count. " +
                            "Expected: $lastOffset, Actual: ${range.first}"
                    }
                    val startTime = timeHelper.now()
                    val identities = loadBiometricIdentities(
                        query = query.copy(afterSubjectId = afterSubjectId), // update query with the last seen subject ID
                        pageSize = range.last - range.first + 1,
                        format = format,
                        createCandidateRecord = createCandidateRecord,
                        onCandidateLoaded = onCandidateLoaded,
                    )
                    afterSubjectId = identities.lastOrNull()?.subjectId
                    lastOffset = range.last + 1
                    val endTime = timeHelper.now()
                    channel.send(CandidateRecordBatch(identities, startTime, endTime))
                }
            channel.close()
        }
        return channel
    }

    private suspend fun loadBiometricIdentities(
        query: EnrolmentRecordQuery,
        pageSize: Int,
        format: String?,
        createCandidateRecord: (subjectId: String, samples: List<DbBiometricTemplate>) -> CandidateRecord,
        onCandidateLoaded: suspend () -> Unit,
    ): List<CandidateRecord> = withContext(dispatcherIO) {
        requireNotNull(format) { "Appropriate sampleFormat is required for loading biometric identities." }
        subjectDao
            .loadSamples(queryBuilder.buildBiometricTemplatesQuery(query, pageSize))
            .map { (subjectId, templates) ->
                onCandidateLoaded()
                createCandidateRecord(subjectId, templates)
            }
    }

    override suspend fun delete(queries: List<EnrolmentRecordQuery>): Unit = withContext(dispatcherIO) {
        Simber.i("[delete] Deleting subjects with queries: $queries", tag = ROOM_RECORDS_DB)
        database.withTransaction {
            queries.forEach { query ->
                subjectDao.deleteSubjects(queryBuilder.buildDeleteQuery(query))
            }
        }
    }

    override suspend fun deleteAll(): Unit = withContext(dispatcherIO) {
        Simber.i("[deleteAll] Deleting all subjects.", tag = ROOM_RECORDS_DB)
        subjectDao.deleteSubjects(queryBuilder.buildDeleteQuery(EnrolmentRecordQuery()))
    }

    override suspend fun performActions(
        actions: List<EnrolmentRecordAction>,
        project: Project,
    ) {
        database.withTransaction {
            actions.forEach { action ->
                when (action) {
                    is EnrolmentRecordAction.Creation -> createSubject(action.subject, project)
                    is EnrolmentRecordAction.Update -> updateSubject(action)
                    is EnrolmentRecordAction.Deletion -> deleteSubject(action.subjectId)
                }
            }
        }
    }

    override suspend fun getLocalDBInfo(): String {
        //  return the data base name and version and number of subjects
        return withContext(dispatcherIO) {
            val dbVersion = database.openHelper.readableDatabase.version
            val dbPath = database.openHelper.readableDatabase.path
            val dbSize = getTotalRoomDbSizeBytes(dbPath!!)
            val isDBEncrypted = DB_ENCRYPTION
            val subjectCount = subjectDao.countSubjects(queryBuilder.buildCountQuery(EnrolmentRecordQuery()))
            "Room DB Info:\n" +
                "Database Name: ${database.openHelper.databaseName}\n" +
                "Database Version: $dbVersion\n" +
                "Database Path: $dbPath\n" +
                "Database Size: ${dbSize / 1024} KB\n" +
                "Is Encrypted: $isDBEncrypted\n" +
                "Number of Subjects: $subjectCount"
        }
    }

    private fun getTotalRoomDbSizeBytes(fullDbPath: String): Long {
        val baseFile = File(fullDbPath)
        val walFile = File("$fullDbPath-wal")
        val shmFile = File("$fullDbPath-shm")

        return listOf(baseFile, walFile, shmFile)
            .filter { it.exists() }
            .sumOf { it.length() }
    }

    private suspend fun createSubject(
        subject: DomainSubject,
        project: Project,
    ) {
        require(subject.references.isNotEmpty()) {
            val errorMsg = "Subject should include at least one sample"
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
        subject.references.takeIf { it.isNotEmpty() }?.map { reference ->
            val dbBiometricTemplates = reference.toRoomDbTemplate(subject.subjectId)
            subjectDao.insertBiometricSamples(dbBiometricTemplates)
        }
        subject.externalCredentials.takeIf { it.isNotEmpty() }?.let { credentials ->
            val dbExternalCredentials = credentials.map { it.toRoomDbCredentials() }
            subjectDao.insertExternalCredentials(dbExternalCredentials)
        }
    }

    private suspend fun updateSubject(action: EnrolmentRecordAction.Update) {
        val dbSubject = subjectDao.getSubject(action.subjectId)
        if (dbSubject != null) {
            val referencesToDelete = action.referenceIdsToRemove.toSet()
            require(
                referencesToDelete.size != dbSubject.biometricTemplates.size ||
                    action.samplesToAdd.isNotEmpty() ||
                    action.externalCredentialsToAdd.isNotEmpty() ||
                    action.externalCredentialIdsToRemove.isNotEmpty(),
            ) {
                val errorMsg = "Cannot delete all samples for subject ${action.subjectId} without adding new ones"
                Simber.i("[updateSubject] $errorMsg", tag = ROOM_RECORDS_DB)
                errorMsg
            }
            dbSubject.biometricTemplates.filter { it.referenceId in referencesToDelete }.forEach {
                subjectDao.deleteBiometricSample(it.uuid)
            }
            val templatesToAdd = action.samplesToAdd.flatMap { it.toRoomDbTemplate(action.subjectId) }
            if (templatesToAdd.isNotEmpty()) {
                subjectDao.insertBiometricSamples(templatesToAdd)
            }
            dbSubject.externalCredentials.filter { it.id in action.externalCredentialIdsToRemove }.forEach {
                subjectDao.deleteExternalCredentialById(it.id)
            }
            if (action.externalCredentialsToAdd.isNotEmpty()) {
                subjectDao.insertExternalCredentials(action.externalCredentialsToAdd.map { it.toRoomDbCredentials() })
            }
        } else {
            Simber.e(
                "[updateSubject] Subject ${action.subjectId} not found for update",
                IllegalStateException(
                    "Subject ${action.subjectId} not found for update",
                ),
                tag = ROOM_RECORDS_DB,
            )
        }
    }

    private suspend fun deleteSubject(subjectId: String) {
        Simber.d("[deleteSubject] Deleting subject: $subjectId", tag = ROOM_RECORDS_DB)
        subjectDao.deleteSubject(subjectId)
    }

    override suspend fun getAllSubjectIds(): List<String> = withContext(dispatcherIO) {
        subjectDao.getAllSubjectIds()
    }

    override suspend fun closeOpenDbConnection() = withContext(dispatcherIO) {
        subjectsDatabaseFactory.get().close()
    }
}
