package com.simprints.feature.externalcredential.screens.search

import android.text.InputType
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.scanocr.usecase.FaydaCardOcrReaderUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GhanaIdCardOcrReaderUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GhanaNhisCardOcrReaderUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.feature.externalcredential.screens.search.model.SearchCredentialState
import com.simprints.feature.externalcredential.screens.search.model.SearchState
import com.simprints.feature.externalcredential.screens.search.usecase.MatchCandidatesUseCase
import com.simprints.feature.externalcredential.usecase.ExternalCredentialEventTrackerUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent.ExternalCredentialConfirmationResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

internal class ExternalCredentialSearchViewModel @AssistedInject constructor(
    @Assisted val scannedCredentialResult: ScannedCredentialResult,
    @Assisted val externalCredentialParams: ExternalCredentialParams,
    private val timeHelper: TimeHelper,
    private val configRepository: ConfigRepository,
    private val matchCandidatesUseCase: MatchCandidatesUseCase,
    private val tokenizationProcessor: TokenizationProcessor,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val eventsTracker: ExternalCredentialEventTrackerUseCase,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(
            scannedCredentialResult: ScannedCredentialResult,
            externalCredentialParams: ExternalCredentialParams,
        ): ExternalCredentialSearchViewModel
    }

    val finishEvent: LiveData<LiveDataEventWithContent<ExternalCredentialSearchResult>>
        get() = _finishEvent
    private val _finishEvent = MutableLiveData<LiveDataEventWithContent<ExternalCredentialSearchResult>>()
    private var state: SearchCredentialState =
        SearchCredentialState.buildInitial(scannedCredentialResult, externalCredentialParams.flowType)
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData(state)
    val stateLiveData: LiveData<SearchCredentialState> = _stateLiveData

    private val confirmationStartTime = timeHelper.now()

    private fun updateState(state: (SearchCredentialState) -> SearchCredentialState) {
        this.state = state(this.state)
    }

    init {
        viewModelScope.launch {
            configRepository.getProject()?.let {
                searchSubjectsLinkedToCredential(it, scannedCredentialResult.credential)
            }
        }
    }

    fun updateConfirmation(isConfirmed: Boolean) {
        updateState { it.copy(isConfirmed = isConfirmed) }
    }

    fun updateIsEditingCredential(isEditing: Boolean) {
        updateState { it.copy(isEditingCredential = isEditing) }
    }

    fun confirmCredentialUpdate(updatedCredential: TokenizableString.Raw) {
        viewModelScope.launch {
            configRepository.getProject()?.let { project ->
                updateState { currentState ->
                    currentState.copy(
                        isConfirmed = false,
                        displayedCredential = updatedCredential,
                    )
                }
                searchSubjectsLinkedToCredential(project, updatedCredential)
            }
        }
    }

    fun getButtonTextResource(
        searchState: SearchState,
        flowType: FlowType,
    ): Int? = when (searchState) {
        // button is not displayed during search
        SearchState.Searching -> null

        is SearchState.CredentialLinked -> when (flowType) {
            FlowType.ENROL -> {
                IDR.string.mfid_action_enrol_anyway
            }

            else -> {
                if (searchState.hasSuccessfulVerifications) {
                    IDR.string.mfid_action_go_to_record
                } else {
                    IDR.string.mfid_action_continue
                }
            }
        }

        SearchState.CredentialNotFound -> when (flowType) {
            FlowType.ENROL -> IDR.string.mfid_action_enrol
            else -> IDR.string.mfid_continue
        }
    }

    private suspend fun searchSubjectsLinkedToCredential(
        project: Project,
        credential: TokenizableString.Raw,
    ) {
        val encryptedCredential = tokenizationProcessor.encrypt(
            decrypted = credential,
            tokenKeyType = TokenKeyType.ExternalCredential,
            project = project,
        ) as TokenizableString.Tokenized
        updateState { it.copy(searchState = SearchState.Searching) }
        val searchStartTime = timeHelper.now()
        val candidates = enrolmentRecordRepository.load(
            EnrolmentRecordQuery(projectId = project.id, externalCredential = encryptedCredential),
        )
        eventsTracker.saveSearchEvent(searchStartTime, scannedCredentialResult.credentialScanId, candidates)

        when {
            candidates.isEmpty() -> {
                updateState { it.copy(searchState = SearchState.CredentialNotFound) }
            }

            else -> {
                val projectConfig = configRepository.getProjectConfiguration()
                val matches = matchCandidatesUseCase(candidates, encryptedCredential, externalCredentialParams, project, projectConfig)

                updateState { state -> state.copy(searchState = SearchState.CredentialLinked(matchResults = matches)) }
            }
        }
    }

    /**
     * Function for QOL improvement. Sets the keyboard to specific [InputType] based on the credential type. QR code and Ghana ID card are
     * alpha-numeric, while the NHIS card memberships contain only digits.
     */
    fun getKeyBoardInputType() = when (scannedCredentialResult.credentialType) {
        // NHIS card membership and Fayda FAN contain only numbers
        ExternalCredentialType.NHISCard -> InputType.TYPE_CLASS_NUMBER
        ExternalCredentialType.FaydaCard -> InputType.TYPE_CLASS_NUMBER
        ExternalCredentialType.GhanaIdCard -> InputType.TYPE_CLASS_TEXT
        ExternalCredentialType.QRCode -> InputType.TYPE_CLASS_TEXT
    }

    fun trackRecapture() {
        viewModelScope.launch {
            eventsTracker.saveConfirmation(
                confirmationStartTime,
                ExternalCredentialConfirmationResult.RECAPTURE,
            )
        }
    }

    fun finish(state: SearchCredentialState) {
        viewModelScope.launch {
            eventsTracker.saveConfirmation(
                confirmationStartTime,
                ExternalCredentialConfirmationResult.CONTINUE,
            )
            val matches = when (val searchState = state.searchState) {
                SearchState.Searching,
                SearchState.CredentialNotFound,
                -> emptyList()

                is SearchState.CredentialLinked -> searchState.matchResults
            }
            _finishEvent.send(
                ExternalCredentialSearchResult.Complete(
                    flowType = externalCredentialParams.flowType,
                    scannedCredentialResult = state.scannedCredentialResult,
                    confirmedCredential = state.displayedCredential,
                    matchResults = matches,
                ),
            )
        }
    }

    fun isCredentialFormatValid(credential: String?): Boolean {
        if (credential == null) return false
        return when (scannedCredentialResult.credentialType) {
            ExternalCredentialType.NHISCard -> {
                // 8 digits
                GhanaNhisCardOcrReaderUseCase.NHIS_PATTERN.matches(credential)
            }
            ExternalCredentialType.GhanaIdCard -> {
                // Ghana ID card number pattern is "GHA-123456789-0"
                GhanaIdCardOcrReaderUseCase.GHANA_ID_PATTERN.matches(credential)
            }
            ExternalCredentialType.FaydaCard -> {
                // Fayda Alias Number (FAN): exactly 16 digits
                FaydaCardOcrReaderUseCase.FAN_PATTERN.matches(credential)
            }
            ExternalCredentialType.QRCode -> {
                // No QR code validation as of 2025.4.1
                true
            }
        }
    }
}
