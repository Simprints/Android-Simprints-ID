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
import com.simprints.feature.enrollast.screen.usecase.BuildRecordUseCase
import com.simprints.feature.enrollast.screen.usecase.CheckForDuplicateEnrolmentsUseCase
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.usecase.ResetExternalCredentialsInSessionUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
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
    private val configRepository: ConfigRepository,
    private val eventRepository: SessionEventRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val checkForDuplicateEnrolments: CheckForDuplicateEnrolmentsUseCase,
    private val tokenizationProcessor: TokenizationProcessor,
    private val buildSubject: BuildRecordUseCase,
    private val resetEnrolmentUpdateEventsFromSession: ResetExternalCredentialsInSessionUseCase,
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
            params.credentialSearchResult?.let { credentialSearchResult ->
                val guidToEnrol = getPreviousEnrolmentResult(params.steps)?.subjectId
                if (isCredentialLinkedToAnotherSubject(
                        confirmedCredential = credentialSearchResult.confirmedCredential,
                        guidToEnrol = guidToEnrol,
                    )
                ) {
                    displayAddCredentialDialog(credentialSearchResult)
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

        val projectConfig = configRepository.getProjectConfiguration()
        val project = configRepository.getProject()
        if (project == null) {
            _finish.send(EnrolLastState.Failed(GENERAL_ERROR, emptyList()))
            return@launch
        }
        val modalities = projectConfig.general.modalities

        val previousLastEnrolmentResult = getPreviousEnrolmentResult(params.steps)
        val credentialSearchResult = params.credentialSearchResult?.takeIf { isAddingCredential }
        if (previousLastEnrolmentResult != null) {
            _finish.send(
                previousLastEnrolmentResult.subjectId
                    ?.let { subjectId -> EnrolLastState.Success(subjectId, credentialSearchResult) }
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
            enrolmentRecordRepository.performActions(listOf(EnrolmentRecordAction.Creation(subject)), project)
            _finish.send(EnrolLastState.Success(subject.subjectId, credentialSearchResult))
        } catch (t: Throwable) {
            Simber.e("Enrolment failed", t, tag = ENROLMENT)
            _finish.send(EnrolLastState.Failed(GENERAL_ERROR, modalities))
        }
    }

    private fun displayAddCredentialDialog(credentialSearchResult: ExternalCredentialSearchResult.Complete) {
        val scannedCredential = credentialSearchResult.scannedCredentialResult
        val confirmedCredential = credentialSearchResult.confirmedCredential
        _showAddCredentialDialog.send(
            CredentialDialogItem(
                scannedCredential,
                confirmedCredential,
            ),
        )
    }

    private suspend fun isCredentialLinkedToAnotherSubject(
        confirmedCredential: TokenizableString.Raw?,
        guidToEnrol: String?,
    ): Boolean {
        if (confirmedCredential == null || guidToEnrol == null) return false
        val project = configRepository.getProject() ?: return false
        val credential = tokenizationProcessor.encrypt(
            decrypted = confirmedCredential,
            tokenKeyType = TokenKeyType.ExternalCredential,
            project = project,
        ) as TokenizableString.Tokenized
        return enrolmentRecordRepository
            .load(
                EnrolmentRecordQuery(
                    projectId = project.id,
                    externalCredential = credential,
                ),
            ).any { it.subjectId != guidToEnrol }
    }

    private fun getPreviousEnrolmentResult(steps: List<EnrolLastBiometricStepResult>) =
        steps.filterIsInstance<EnrolLastBiometricStepResult.EnrolLastBiometricsResult>().firstOrNull()

    private suspend fun registerEvent(enrolmentRecord: EnrolmentRecord) {
        Simber.d("Register events for enrolments", tag = ENROLMENT)
        val events = eventRepository.getEventsInCurrentSession()

        // Ensures that any previous confirmations are removed from session
        resetEnrolmentUpdateEventsFromSession()

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
                subjectId = enrolmentRecord.subjectId,
                projectId = enrolmentRecord.projectId,
                moduleId = enrolmentRecord.moduleId,
                attendantId = enrolmentRecord.attendantId,
                biometricReferenceIds = biometricReferenceIds,
                externalCredentialIds = externalCredentialIds,
            ),
        )
    }
}
