package com.simprints.feature.selectsubject.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.SessionCoroutineScope
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.extentions.isValidGuid
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.toExternalCredential
import com.simprints.feature.externalcredential.usecase.ResetExternalCredentialsInSessionUseCase
import com.simprints.feature.selectsubject.SelectSubjectParams
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.feature.selectsubject.model.SelectSubjectState
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.event.domain.models.EnrolmentUpdateEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SESSION
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class SelectSubjectViewModel @AssistedInject constructor(
    @Assisted private val params: SelectSubjectParams,
    private val timeHelper: TimeHelper,
    private val authStore: AuthStore,
    private val eventRepository: SessionEventRepository,
    private val configManager: ConfigManager,
    private val resetExternalCredentialsUseCase: ResetExternalCredentialsInSessionUseCase,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val tokenizationProcessor: TokenizationProcessor,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(selectSubjectParams: SelectSubjectParams): SelectSubjectViewModel
    }

    private var state: SelectSubjectState = SelectSubjectState.EMPTY
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData<SelectSubjectState>()
    val stateLiveData: LiveData<SelectSubjectState> = _stateLiveData
    val finish: LiveData<LiveDataEventWithContent<SelectSubjectResult>>
        get() = _finish
    private var _finish = MutableLiveData<LiveDataEventWithContent<SelectSubjectResult>>()

    private fun updateState(state: (SelectSubjectState) -> SelectSubjectState) {
        this.state = state(this.state)
    }

    init {
        viewModelScope.launch {
            val isSaved = saveGuidSelection(projectId = params.projectId, subjectId = params.subjectId)
            if (!isSaved) {
                _finish.send(SelectSubjectResult(isSubjectIdSaved = false, savedCredential = null))
                return@launch
            }

            val dialogDisplayedState = getDisplayDialogStateIfRequired(params.scannedCredential, params.subjectId)
            if (dialogDisplayedState != null) {
                updateState { dialogDisplayedState }
            } else {
                val credential = params.scannedCredential?.toExternalCredential(params.subjectId)
                _finish.send(SelectSubjectResult(isSubjectIdSaved = true, savedCredential = credential))
            }
        }
    }

    private suspend fun saveGuidSelection(
        projectId: String,
        subjectId: String,
    ): Boolean {
        updateState { SelectSubjectState.SavingSubjectId }
        if (!authStore.isProjectIdSignedIn(projectId)) return false
        return saveSelectionEvent(subjectId)
    }

    private suspend fun getDisplayDialogStateIfRequired(
        scannedCredential: ScannedCredential?,
        subjectId: String,
    ): SelectSubjectState.CredentialDialogDisplayed? {
        if (scannedCredential == null) return null
        val credential = scannedCredential.credential
        val project = configManager.getProject() ?: return null
        val alreadyLinkedSubject = enrolmentRecordRepository
            .load(
                SubjectQuery(
                    projectId = project.id,
                    subjectId = subjectId,
                    externalCredential = credential,
                ),
            ).firstOrNull()

        if (!subjectId.isValidGuid()) {
            // Confirmation of "none_selected" (or any non UUID value) should not display the dialog,
            // but still remove update event from session and reset previously linked external credentials
            resetExternalCredentialsUseCase(
                scannedCredential = scannedCredential,
                subjectId = params.subjectId,
            )
            return null
        }

        // Credentials already linked to the correct subject, so no need to re-link
        if (alreadyLinkedSubject != null && alreadyLinkedSubject.subjectId == subjectId) return null

        val decrypted = tokenizationProcessor.decrypt(
            encrypted = credential,
            tokenKeyType = TokenKeyType.ExternalCredential,
            project = project,
        ) as TokenizableString.Raw
        return SelectSubjectState.CredentialDialogDisplayed(scannedCredential = scannedCredential, displayedCredential = decrypted)
    }

    fun saveCredential(scannedCredential: ScannedCredential) {
        updateState { SelectSubjectState.SavingExternalCredential }
        viewModelScope.launch {
            val addedCredential = try {
                resetExternalCredentialsUseCase(
                    scannedCredential = scannedCredential,
                    subjectId = params.subjectId,
                )

                // Confirmation of "none_selected" (or any non UUID value) should not produce an EnrolmentUpdateEvent
                if (params.subjectId.isValidGuid()) {
                    saveCredentialSelectionEvent(params.subjectId)
                }
                scannedCredential
            } catch (e: Exception) {
                Simber.e("Failed to attach scanned credential", e, tag = SESSION)
                null
            }
            _finish.send(
                SelectSubjectResult(
                    isSubjectIdSaved = true,
                    savedCredential = addedCredential?.toExternalCredential(params.subjectId),
                ),
            )
        }
    }

    fun finishWithoutSavingCredential() {
        _finish.send(SelectSubjectResult(isSubjectIdSaved = true, savedCredential = null))
    }

    private suspend fun saveCredentialSelectionEvent(subjectId: String) = with(sessionCoroutineScope) {
        try {
            val externalCredentialIdsToAdd = eventRepository
                .getEventsInCurrentSession()
                .filterIsInstance<ExternalCredentialCaptureValueEvent>()
                .map { it.payload.id }

            Simber.d("Adding credentials $externalCredentialIdsToAdd to subject $subjectId", tag = SESSION)

            eventRepository.addOrUpdateEvent(
                EnrolmentUpdateEvent(
                    timeHelper.now(),
                    subjectId = subjectId,
                    externalCredentialIdsToAdd = externalCredentialIdsToAdd,
                ),
            )
        } catch (t: Throwable) {
            Simber.e("Failed to save Enrolment Update Event", t, tag = SESSION)
        }
    }

    private suspend fun saveSelectionEvent(subjectId: String): Boolean = with(sessionCoroutineScope) {
        try {
            val event = GuidSelectionEvent(timeHelper.now(), subjectId)
            eventRepository.addOrUpdateEvent(event)

            Simber.i("Added Guid Selection Event", tag = SESSION)
            return true
        } catch (t: Throwable) {
            // It doesn't matter if it was an error, we always return a result
            Simber.e("Failed to save Guid Selection Event", t, tag = SESSION)
            return false
        }
    }
}
