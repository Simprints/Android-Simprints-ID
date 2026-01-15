package com.simprints.infra.enrolment.records.repository

import androidx.core.content.edit
import com.simprints.core.DispatcherIO
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.exceptions.RealmUninitialisedException
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.enrolment.records.repository.local.SelectEnrolmentRecordLocalDataSourceUseCase
import com.simprints.infra.enrolment.records.repository.local.migration.InsertRecordsInRoomDuringMigrationUseCase
import com.simprints.infra.enrolment.records.repository.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EnrolmentRecordRepositoryImpl @Inject constructor(
    private val remoteDataSource: EnrolmentRecordRemoteDataSource,
    @param:CommCareDataSource private val commCareDataSource: CandidateRecordDataSource,
    private val tokenizationProcessor: TokenizationProcessor,
    private val selectEnrolmentRecordLocalDataSource: SelectEnrolmentRecordLocalDataSourceUseCase,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher,
    @param:EnrolmentBatchSize private val batchSize: Int,
    private val insertRecordsInRoomDuringMigration: InsertRecordsInRoomDuringMigrationUseCase,
    securityManager: SecurityManager,
) : EnrolmentRecordRepository {
    private val prefs = securityManager.buildEncryptedSharedPreferences(PREF_FILE_NAME)

    companion object {
        private const val PREF_FILE_NAME = "UPLOAD_ENROLMENT_RECORDS_PROGRESS"
        private const val PROGRESS_KEY = "PROGRESS"
    }

    override suspend fun uploadRecords(subjectIds: List<String>) = withContext(dispatcher) {
        val lastUploadedRecord = prefs.getString(PROGRESS_KEY, null)
        var query = EnrolmentRecordQuery(sort = true, afterSubjectId = lastUploadedRecord)
        if (subjectIds.isNotEmpty()) {
            query = EnrolmentRecordQuery(
                subjectIds = subjectIds,
                sort = true,
                afterSubjectId = lastUploadedRecord,
            )
        }
        selectEnrolmentRecordLocalDataSource()
            .load(query)
            .chunked(batchSize)
            .forEach { subjects ->
                remoteDataSource.uploadRecords(subjects)
                prefs.edit { putString(PROGRESS_KEY, subjects.last().subjectId) }
            }
        prefs.edit { remove(PROGRESS_KEY) }
    }

    override suspend fun tokenizeExistingRecords(project: Project) {
        try {
            val query = EnrolmentRecordQuery(projectId = project.id, hasUntokenizedFields = true)
            val tokenizedRecordsCreateAction = selectEnrolmentRecordLocalDataSource()
                .load(query)
                .mapNotNull { record ->
                    if (record.projectId != project.id) return@mapNotNull null
                    val moduleId = tokenizationProcessor.tokenizeIfNecessary(
                        tokenizableString = record.moduleId,
                        tokenKeyType = TokenKeyType.ModuleId,
                        project = project,
                    )
                    val attendantId = tokenizationProcessor.tokenizeIfNecessary(
                        tokenizableString = record.attendantId,
                        tokenKeyType = TokenKeyType.AttendantId,
                        project = project,
                    )
                    record.copy(moduleId = moduleId, attendantId = attendantId)
                }.map(EnrolmentRecordAction::Creation)
            selectEnrolmentRecordLocalDataSource().performActions(tokenizedRecordsCreateAction, project)
        } catch (e: Exception) {
            when (e) {
                is RealmUninitialisedException -> Unit

                // AuthStore hasn't yet saved the project, no need to do anything
                else -> Simber.e("Failed to tokenize existing records", e)
            }
        }
    }

    override suspend fun count(
        query: EnrolmentRecordQuery,
        dataSource: BiometricDataSource,
    ): Int = fromCandidateDataSource(dataSource).count(query, dataSource)

    override suspend fun observeCount(
        query: EnrolmentRecordQuery,
        dataSource: BiometricDataSource,
    ): Flow<Int> = fromCandidateDataSource(dataSource).observeCount(query, dataSource)

    override suspend fun getLocalDBInfo(): String = selectEnrolmentRecordLocalDataSource().getLocalDBInfo()

    override suspend fun loadCandidateRecords(
        query: EnrolmentRecordQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ) = fromCandidateDataSource(dataSource).loadCandidateRecords(
        query = query,
        ranges = ranges,
        dataSource = dataSource,
        project = project,
        scope = scope,
        onCandidateLoaded = onCandidateLoaded,
    )

    private suspend fun fromCandidateDataSource(dataSource: BiometricDataSource) = when (dataSource) {
        is BiometricDataSource.Simprints -> selectEnrolmentRecordLocalDataSource()
        is BiometricDataSource.CommCare -> commCareDataSource
    }

    override suspend fun load(query: EnrolmentRecordQuery): List<EnrolmentRecord> = selectEnrolmentRecordLocalDataSource().load(query)

    override suspend fun getAllSubjectIds(): List<String> = selectEnrolmentRecordLocalDataSource().getAllSubjectIds()

    override suspend fun delete(queries: List<EnrolmentRecordQuery>) = selectEnrolmentRecordLocalDataSource().delete(queries)

    override suspend fun deleteAll() = selectEnrolmentRecordLocalDataSource().deleteAll()

    override suspend fun performActions(
        actions: List<EnrolmentRecordAction>,
        project: Project,
    ) {
        insertRecordsInRoomDuringMigration(actions, project)
        selectEnrolmentRecordLocalDataSource().performActions(actions, project)
    }

    override suspend fun closeOpenDbConnection() = selectEnrolmentRecordLocalDataSource().closeOpenDbConnection()
}
