package com.simprints.feature.enrollast.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.screen.EnrolLastState.ErrorType.GENERAL_ERROR
import com.simprints.feature.enrollast.screen.model.CredentialDialogItem
import com.simprints.feature.enrollast.screen.usecase.BuildSubjectUseCase
import com.simprints.feature.enrollast.screen.usecase.CheckForDuplicateEnrolmentsUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.toExternalCredential
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.EnrolmentEventV4
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ENROLMENT
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EnrolLastBiometricViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
    private val eventRepository: SessionEventRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val checkForDuplicateEnrolments: CheckForDuplicateEnrolmentsUseCase,
    private val tokenizationProcessor: TokenizationProcessor,
    private val buildSubject: BuildSubjectUseCase,
) : ViewModel() {
    val finish: LiveData<LiveDataEventWithContent<EnrolLastState>>
        get() = _finish
    private val _finish = MutableLiveData<LiveDataEventWithContent<EnrolLastState>>()

    val showAddCredentialDialog: LiveData<LiveDataEventWithContent<CredentialDialogItem>>
        get() = _showAddCredentialDialog
    private val _showAddCredentialDialog = MutableLiveData<LiveDataEventWithContent<CredentialDialogItem>>()

    private var enrolWasAttempted = false

    fun onViewCreated(params: EnrolLastBiometricParams) {
        viewModelScope.launch {
            params.scannedCredential?.let { scannedCredential ->
                if (isCredentialLinkedToAnotherSubject(scannedCredential, params.projectId)) {
                    displayAddCredentialDialog(scannedCredential, params.projectId)
                    return@launch
                }
            }
            if (!enrolWasAttempted) {
                enrolBiometric(params, isAddingCredential = true)
            }
        }
    }

    fun enrolBiometric(
        params: EnrolLastBiometricParams,
        isAddingCredential: Boolean,
    ) = viewModelScope.launch {
        enrolWasAttempted = true

        val projectConfig = configManager.getProjectConfiguration()
        val project = configManager.getProject(projectConfig.projectId)
        val modalities = projectConfig.general.modalities

        val previousLastEnrolmentResult = getPreviousEnrolmentResult(params.steps)
        val scannedCredential = params.scannedCredential?.takeIf { isAddingCredential }
        if (previousLastEnrolmentResult != null) {
            _finish.send(
                previousLastEnrolmentResult.subjectId
                    ?.let { subjectId -> EnrolLastState.Success(subjectId, scannedCredential?.toExternalCredential(subjectId)) }
                    ?: EnrolLastState.Failed(GENERAL_ERROR, modalities),
            )
            return@launch
        }
        val duplicateEnrolmentError = checkForDuplicateEnrolments(projectConfig, params.steps)
        if (duplicateEnrolmentError != null) {
            _finish.send(EnrolLastState.Failed(duplicateEnrolmentError, modalities))
            return@launch
        }

        try {
            val subject = buildSubject(params, isAddingCredential = isAddingCredential)
            registerEvent(subject)
            enrolmentRecordRepository.performActions(listOf(SubjectAction.Creation(subject)), project)
            _finish.send(EnrolLastState.Success(subject.subjectId, scannedCredential?.toExternalCredential(subject.subjectId)))
        } catch (t: Throwable) {
            Simber.e("Enrolment failed", t, tag = ENROLMENT)
            _finish.send(EnrolLastState.Failed(GENERAL_ERROR, modalities))
        }
    }

    private suspend fun displayAddCredentialDialog(
        scannedCredential: ScannedCredential,
        projectId: String,
    ) {
        val project = configManager.getProject(projectId)
        val decrypted = tokenizationProcessor.decrypt(
            encrypted = scannedCredential.credential,
            tokenKeyType = TokenKeyType.ExternalCredential,
            project = project,
        ) as TokenizableString.Raw
        _showAddCredentialDialog.send(CredentialDialogItem(scannedCredential, decrypted))
    }

    private suspend fun isCredentialLinkedToAnotherSubject(
        scannedCredential: ScannedCredential?,
        projectId: String,
    ): Boolean {
        if (scannedCredential == null) return false

        return enrolmentRecordRepository
            .load(
                SubjectQuery(
                    projectId = projectId,
                    externalCredential = scannedCredential.credential,
                ),
            ).isNotEmpty()
    }

    private fun getPreviousEnrolmentResult(steps: List<EnrolLastBiometricStepResult>) =
        steps.filterIsInstance<EnrolLastBiometricStepResult.EnrolLastBiometricsResult>().firstOrNull()

    private suspend fun registerEvent(subject: Subject) {
        Simber.d("Register events for enrolments", tag = ENROLMENT)
        val events = eventRepository
            .getEventsInCurrentSession()

        val biometricReferenceIds = events
            .filterIsInstance<BiometricReferenceCreationEvent>()
            .sortedByDescending { it.payload.createdAt }
            .map { it.payload.id }

        val externalCredentialIds = events
            .filterIsInstance<ExternalCredentialCaptureValueEvent>()
            .map { it.payload.id }

        eventRepository.addOrUpdateEvent(
            EnrolmentEventV4(
                createdAt = timeHelper.now(),
                subjectId = subject.subjectId,
                projectId = subject.projectId,
                moduleId = subject.moduleId,
                attendantId = subject.attendantId,
                biometricReferenceIds = biometricReferenceIds,
                externalCredentialIds = externalCredentialIds,
            ),
        )
    }
}
