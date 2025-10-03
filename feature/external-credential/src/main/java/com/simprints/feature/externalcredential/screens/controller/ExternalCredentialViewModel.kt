package com.simprints.feature.externalcredential.screens.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@HiltViewModel
internal class ExternalCredentialViewModel @Inject internal constructor() : ViewModel() {

    private var isInitialized = false
    lateinit var params: ExternalCredentialParams
        private set
    val finishEvent: LiveData<LiveDataEventWithContent<ExternalCredentialSearchResult>>
        get() = _finishEvent
    private val _finishEvent = MutableLiveData<LiveDataEventWithContent<ExternalCredentialSearchResult>>()
    private var state: ExternalCredentialState = ExternalCredentialState.EMPTY
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData(ExternalCredentialState.EMPTY)
    val stateLiveData: LiveData<ExternalCredentialState> = _stateLiveData

    private fun updateState(state: (ExternalCredentialState) -> ExternalCredentialState) {
        this.state = state(this.state)
    }

    fun setSelectedExternalCredentialType(selectedType: ExternalCredentialType?) {
        updateState { it.copy(selectedType = selectedType) }
    }

    fun setExternalCredentialValue(value: String) {
        updateState { it.copy(credentialValue = value) }
    }

    fun init(params: ExternalCredentialParams) {
        if (!isInitialized) {
            isInitialized = true
            this.params = params
            updateState { ExternalCredentialState.EMPTY.copy(subjectId = params.subjectId, flowType = params.flowType) }
        }
    }


    fun mapTypeToStringResource(type: ExternalCredentialType?) = when (type) {
        ExternalCredentialType.NHISCard -> IDR.string.mfid_type_nhis_card
        ExternalCredentialType.GhanaIdCard -> IDR.string.mfid_type_ghana_id_card
        ExternalCredentialType.QRCode -> IDR.string.mfid_type_qr_code
        null -> IDR.string.mfid_type_any_document
    }

    fun mapTypeToCredentialFieldResource(type: ExternalCredentialType) = when (type) {
        ExternalCredentialType.NHISCard -> IDR.string.mfid_nhis_card_credential_field
        ExternalCredentialType.GhanaIdCard -> IDR.string.mfid_ghana_id_credential_field
        ExternalCredentialType.QRCode -> IDR.string.mfid_qr_credential_field
    }

    fun finish(result: ExternalCredentialSearchResult) {
        _finishEvent.send(result)
    }
}
