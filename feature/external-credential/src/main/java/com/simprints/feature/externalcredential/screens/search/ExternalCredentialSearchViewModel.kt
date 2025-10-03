package com.simprints.feature.externalcredential.screens.search

import android.text.InputType
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.SearchCredentialState
import com.simprints.feature.externalcredential.screens.search.model.SearchState
import com.simprints.feature.externalcredential.screens.search.usecase.FindSubjectsByCredentialUseCase
import com.simprints.feature.externalcredential.screens.search.usecase.MatchCandidatesUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

internal class ExternalCredentialSearchViewModel @AssistedInject constructor(
    @Assisted val scannedCredential: ScannedCredential,
    @Assisted val externalCredentialParams: ExternalCredentialParams,
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
    private val findSubjectsByCredentialUseCase: FindSubjectsByCredentialUseCase,
    private val matchCandidatesUseCase: MatchCandidatesUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            scannedCredential: ScannedCredential,
            externalCredentialParams: ExternalCredentialParams
        ): ExternalCredentialSearchViewModel
    }


    val finishEvent: LiveData<LiveDataEventWithContent<ExternalCredentialSearchResult>>
        get() = _finishEvent
    private val _finishEvent = MutableLiveData<LiveDataEventWithContent<ExternalCredentialSearchResult>>()
    private var state: SearchCredentialState =
        SearchCredentialState.buildInitial(scannedCredential, externalCredentialParams.flowType)
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData(state)
    val stateLiveData: LiveData<SearchCredentialState> = _stateLiveData
    private fun updateState(state: (SearchCredentialState) -> SearchCredentialState) {
        this.state = state(this.state)
    }

    init {
        viewModelScope.launch {
            searchSubjectsLinkedToCredential(scannedCredential.credential)
        }
    }

    fun updateConfirmation(isConfirmed: Boolean) {
        updateState { it.copy(isConfirmed = isConfirmed) }
    }

    fun updateCredentialValue(updatedCredential: String) {
        viewModelScope.launch {
            updateState { currentState ->
                currentState.copy(
                    isConfirmed = false,
                    scannedCredential = currentState.scannedCredential.copy(
                        credential = updatedCredential
                    )
                )
            }
            searchSubjectsLinkedToCredential(updatedCredential)
        }
    }

    fun getButtonTextResource(searchState: SearchState, flowType: FlowType): Int? =
        when (searchState) {
            SearchState.Searching -> null // button is not displayed during search
            is SearchState.CredentialLinked -> when (flowType) {
                FlowType.ENROL -> IDR.string.mfid_action_enrol_anyway
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

    private suspend fun searchSubjectsLinkedToCredential(credential: String) {
        updateState { it.copy(searchState = SearchState.Searching) }
        val project = configManager.getProject(authStore.signedInProjectId)
        val candidates = findSubjectsByCredentialUseCase(credential, project)
        when {
            candidates.isEmpty() -> updateState { it.copy(searchState = SearchState.CredentialNotFound) }
            else -> {
                val projectConfig = configManager.getProjectConfiguration()
                val matches = matchCandidatesUseCase(candidates, credential, externalCredentialParams, project, projectConfig)
                updateState { state -> state.copy(searchState = SearchState.CredentialLinked(matchResults = matches)) }
            }
        }
    }

    /**
     * Function for QOL improvement. Sets the keyboard to specific [InputType] based on the credential type. QR code and Ghana ID card are
     * alpha-numeric, while the NHIS card memberships contain only digits.
     */
    fun getKeyBoardInputType() = when (scannedCredential.credentialType) {
        ExternalCredentialType.NHISCard -> InputType.TYPE_CLASS_NUMBER // NHIS card membership contains only numbers
        ExternalCredentialType.GhanaIdCard -> InputType.TYPE_CLASS_TEXT
        ExternalCredentialType.QRCode -> InputType.TYPE_CLASS_TEXT
    }

    fun finish(searchState: SearchState) {
        val matches = when (searchState) {
            SearchState.Searching,
            SearchState.CredentialNotFound -> emptyList()

            is SearchState.CredentialLinked -> searchState.matchResults
        }
        _finishEvent.send(
            ExternalCredentialSearchResult(
                flowType = externalCredentialParams.flowType,
                scannedCredential = scannedCredential,
                matchResults = matches,
            )
        )
    }

}
