package com.simprints.infra.enrolment.records.store

import android.content.Context
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.store.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.logging.Simber
import com.simprints.infra.realm.exceptions.RealmUninitialisedException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class EnrolmentRecordRepositoryImpl(
    context: Context,
    private val remoteDataSource: EnrolmentRecordRemoteDataSource,
    private val subjectRepository: SubjectRepository,
    private val tokenizationProcessor: TokenizationProcessor,
    private val dispatcher: CoroutineDispatcher,
    private val batchSize: Int,
) : EnrolmentRecordRepository {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        remoteDataSource: EnrolmentRecordRemoteDataSource,
        subjectRepository: SubjectRepository,
        tokenizationProcessor: TokenizationProcessor,
        @DispatcherIO dispatcher: CoroutineDispatcher,
    ) : this(
        context = context,
        remoteDataSource = remoteDataSource,
        subjectRepository = subjectRepository,
        tokenizationProcessor = tokenizationProcessor,
        dispatcher = dispatcher,
        batchSize = BATCH_SIZE
    )

    private val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    companion object {

        private const val BATCH_SIZE = 80
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
                afterSubjectId = lastUploadedRecord
            )
        }
        subjectRepository.load(query)
            .chunked(batchSize)
            .forEach { subjects ->
                remoteDataSource.uploadRecords(subjects)
                prefs.edit().putString(PROGRESS_KEY, subjects.last().subjectId).apply()
            }
        prefs.edit().remove(PROGRESS_KEY).apply()
    }

    override suspend fun tokenizeExistingRecords(project: Project) {
        try {
            val query = SubjectQuery(projectId = project.id, hasUntokenizedFields = true)
            val tokenizedSubjectsCreateAction =
                subjectRepository.load(query).mapNotNull { subject ->
                    if (subject.projectId != project.id) return@mapNotNull null
                    val moduleId = tokenizeIfNecessary(
                        value = subject.moduleId,
                        tokenKeyType = TokenKeyType.ModuleId,
                        project = project
                    )
                    val attendantId = tokenizeIfNecessary(
                        value = subject.attendantId,
                        tokenKeyType = TokenKeyType.AttendantId,
                        project = project
                    )
                    return@mapNotNull subject.copy(moduleId = moduleId, attendantId = attendantId)
                }.map(SubjectAction::Creation)
            subjectRepository.performActions(tokenizedSubjectsCreateAction)
        } catch (e: Exception) {
            when (e) {
                is RealmUninitialisedException -> Unit // AuthStore hasn't yet saved the project, no need to do anything
                else -> Simber.e(e)
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
            project = project
        )
    }
}
