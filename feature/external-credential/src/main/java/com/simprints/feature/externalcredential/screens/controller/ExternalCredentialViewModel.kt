package com.simprints.feature.externalcredential.screens.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@HiltViewModel
internal class ExternalCredentialViewModel @Inject internal constructor(

) : ViewModel() {

    private var state: ExternalCredentialState = ExternalCredentialState.EMPTY
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData<ExternalCredentialState>()
    val stateLiveData: LiveData<ExternalCredentialState> = _stateLiveData

    fun setSelectedExternalCredentialType(selectedType: ExternalCredentialType?) {
        updateState { it.copy(selectedType = selectedType) }
    }

    private fun updateState(state: (ExternalCredentialState) -> ExternalCredentialState) {
        this.state = state(this.state)
    }

    fun mapTypeToStringResource(type: ExternalCredentialType) = when(type) {
        ExternalCredentialType.NHISCard -> IDR.string.mfid_type_nhis_card
        ExternalCredentialType.GhanaIdCard -> IDR.string.mfid_type_ghana_id_card
        ExternalCredentialType.QRCode -> IDR.string.mfid_type_qr_code
    }
}
