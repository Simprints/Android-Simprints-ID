package com.simprints.feature.externalcredential.screens.search

import android.text.InputType
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.SearchCredentialState
import com.simprints.feature.externalcredential.screens.search.model.SearchState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import com.simprints.infra.resources.R as IDR

internal class ExternalCredentialSearchViewModel @AssistedInject constructor(
    @Assisted val scannedCredential: ScannedCredential,
    @Assisted val flowType: FlowType,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            scannedCredential: ScannedCredential,
            flowType: FlowType
        ): ExternalCredentialSearchViewModel
    }

    private var state: SearchCredentialState =
        SearchCredentialState.buildInitial(scannedCredential, flowType)
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData(state)
    val stateLiveData: LiveData<SearchCredentialState> = _stateLiveData
    private fun updateState(state: (SearchCredentialState) -> SearchCredentialState) {
        this.state = state(this.state)
    }

    fun updateConfirmation(isConfirmed: Boolean) {
        updateState { it.copy(isConfirmed = isConfirmed) }
    }

    fun updateCredentialValue(updatedCredential: String) {
        updateState { currentState ->
            currentState.copy(
                isConfirmed = false,
                scannedCredential = currentState.scannedCredential.copy(
                    credential = updatedCredential
                )
            )
        }
    }

    fun getButtonTextResource(searchState: SearchState, flowType: FlowType): Int? =
        when (searchState) {
            SearchState.Searching -> null // button is not displayed during search
            is SearchState.SubjectFound -> when (flowType) {
                FlowType.ENROL -> IDR.string.mfid_action_enrol_anyway
                else -> IDR.string.mfid_action_go_to_record
            }

            SearchState.SubjectNotFound -> when (flowType) {
                FlowType.ENROL -> IDR.string.mfid_action_enrol
                else -> IDR.string.mfid_continue
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

}
