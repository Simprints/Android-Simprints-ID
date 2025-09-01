package com.simprints.infra.enrolment.records.repository

import androidx.core.content.edit
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.exceptions.RealmUninitialisedException
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.SelectEnrolmentRecordLocalDataSourceUseCase
import com.simprints.infra.enrolment.records.repository.local.migration.InsertRecordsInRoomDuringMigrationUseCase
import com.simprints.infra.enrolment.records.repository.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class EnrolmentRecordRepositoryImpl @Inject constructor(
    private val remoteDataSource: EnrolmentRecordRemoteDataSource,
    @CommCareDataSource private val commCareDataSource: IdentityDataSource,
    private val tokenizationProcessor: TokenizationProcessor,
    private val selectEnrolmentRecordLocalDataSource: SelectEnrolmentRecordLocalDataSourceUseCase,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
    @EnrolmentBatchSize private val batchSize: Int,
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
        var query = SubjectQuery(sort = true, afterSubjectId = lastUploadedRecord)
        if (subjectIds.isNotEmpty()) {
            query = SubjectQuery(
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
            val query = SubjectQuery(projectId = project.id, hasUntokenizedFields = true)
            val tokenizedSubjectsCreateAction = selectEnrolmentRecordLocalDataSource()
                .load(query)
                .mapNotNull { subject ->
                    if (subject.projectId != project.id) return@mapNotNull null
                    val moduleId = tokenizeIfNecessary(
                        value = subject.moduleId,
                        tokenKeyType = TokenKeyType.ModuleId,
                        project = project,
                    )
                    val attendantId = tokenizeIfNecessary(
                        value = subject.attendantId,
                        tokenKeyType = TokenKeyType.AttendantId,
                        project = project,
                    )
                    return@mapNotNull subject.copy(moduleId = moduleId, attendantId = attendantId)
                }.map(SubjectAction::Creation)
            selectEnrolmentRecordLocalDataSource().performActions(tokenizedSubjectsCreateAction, project)
        } catch (e: Exception) {
            when (e) {
                is RealmUninitialisedException -> Unit // AuthStore hasn't yet saved the project, no need to do anything
                else -> Simber.e("Failed to tokenize existing records", e)
            }
        }
    }

    private fun tokenizeIfNecessary(
        value: TokenizableString,
        tokenKeyType: TokenKeyType,
        project: Project,
    ) = when (value) {
        is TokenizableString.Tokenized -> value
        is TokenizableString.Raw -> tokenizationProcessor.encrypt(
            decrypted = value,
            tokenKeyType = tokenKeyType,
            project = project,
        )
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = fromIdentityDataSource(dataSource).count(query, dataSource)

    override suspend fun getLocalDBInfo(): String = selectEnrolmentRecordLocalDataSource().getLocalDBInfo()

    override suspend fun loadIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ) = fromIdentityDataSource(dataSource).loadIdentities(
        query = query,
        ranges = ranges,
        dataSource = dataSource,
        project = project,
        scope = scope,
        onCandidateLoaded = onCandidateLoaded,
    )

    private suspend fun fromIdentityDataSource(dataSource: BiometricDataSource) = when (dataSource) {
        is BiometricDataSource.Simprints -> selectEnrolmentRecordLocalDataSource()
        is BiometricDataSource.CommCare -> commCareDataSource
    }

    override suspend fun load(query: SubjectQuery): List<Subject> = selectEnrolmentRecordLocalDataSource().load(query)

    override suspend fun getAllSubjectIds(): List<String> = selectEnrolmentRecordLocalDataSource().getAllSubjectIds()

    override suspend fun delete(queries: List<SubjectQuery>) = selectEnrolmentRecordLocalDataSource().delete(queries)

    override suspend fun deleteAll() = selectEnrolmentRecordLocalDataSource().deleteAll()

    override suspend fun performActions(
        actions: List<SubjectAction>,
        project: Project,
    ) {
        insertRecordsInRoomDuringMigration(actions, project)
        selectEnrolmentRecordLocalDataSource().performActions(actions, project)
    }
}
