package com.simprints.infra.enrolment.records.repository

import android.content.Context
import androidx.core.content.edit
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.exceptions.RealmUninitialisedException
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSource
import com.simprints.infra.enrolment.records.repository.local.SelectEnrolmentRecordLocalDataSourceUseCase
import com.simprints.infra.enrolment.records.repository.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class EnrolmentRecordRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val remoteDataSource: EnrolmentRecordRemoteDataSource,
    @CommCareDataSource private val commCareDataSource: IdentityDataSource,
    private val tokenizationProcessor: TokenizationProcessor,
    private val selectEnrolmentRecordLocalDataSource: SelectEnrolmentRecordLocalDataSourceUseCase,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
    @EnrolmentBatchSize private val batchSize: Int,
) : EnrolmentRecordRepository {
    private val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_FILE_NAME = "UPLOAD_ENROLMENT_RECORDS_PROGRESS"
        private const val PROGRESS_KEY = "PROGRESS"
    }

    private val localDataSource: EnrolmentRecordLocalDataSource by lazy { selectEnrolmentRecordLocalDataSource() }

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
        localDataSource.load(query).chunked(batchSize).forEach { subjects ->
            remoteDataSource.uploadRecords(subjects)
            prefs.edit { putString(PROGRESS_KEY, subjects.last().subjectId) }
        }
        prefs.edit { remove(PROGRESS_KEY) }
    }

    override suspend fun tokenizeExistingRecords(project: Project) {
        try {
            val query = SubjectQuery(projectId = project.id, hasUntokenizedFields = true)
            val tokenizedSubjectsCreateAction = localDataSource
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
            localDataSource.performActions(tokenizedSubjectsCreateAction, project)
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

    override fun loadFingerprintIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ) = fromIdentityDataSource(dataSource).loadFingerprintIdentities(
        query = query,
        ranges = ranges,
        dataSource = dataSource,
        project = project,
        scope = scope,
        onCandidateLoaded = onCandidateLoaded,
    )

    override fun loadFaceIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ) = fromIdentityDataSource(dataSource).loadFaceIdentities(
        query = query,
        ranges = ranges,
        dataSource = dataSource,
        project = project,
        scope = scope,
        onCandidateLoaded = onCandidateLoaded,
    )

    private fun fromIdentityDataSource(dataSource: BiometricDataSource) = when (dataSource) {
        is BiometricDataSource.Simprints -> localDataSource
        is BiometricDataSource.CommCare -> commCareDataSource
    }

    override suspend fun load(query: SubjectQuery): List<Subject> = localDataSource.load(query)

    override suspend fun delete(queries: List<SubjectQuery>) = localDataSource.delete(queries)

    override suspend fun deleteAll() = localDataSource.deleteAll()

    override suspend fun performActions(
        actions: List<SubjectAction>,
        project: Project,
    ) = localDataSource.performActions(actions, project)
}
