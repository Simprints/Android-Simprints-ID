package com.simprints.feature.externalcredential.screens.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.common.FlowType
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.externalcredential.model.ExternalCredentialValidation
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.model.ExternalCredentialResult
import com.simprints.feature.externalcredential.model.ExternalCredentialSaveResponse
import com.simprints.feature.externalcredential.model.ExternalCredentialSearchResponse
import com.simprints.feature.externalcredential.model.ExternalCredentialSkipResponse
import com.simprints.infra.external.credential.store.model.ExternalCredential
import com.simprints.infra.external.credential.store.repository.ExternalCredentialRepository
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialViewModel @Inject constructor(
    private val externalCredentialRepository: ExternalCredentialRepository
) : ViewModel() {

    private var subjectId: String? = null
    private var flowType: FlowType? = null

    val externalCredentialSaveResponse: LiveData<LiveDataEventWithContent<Serializable>>
        get() = _externalCredentialSaveResponse
    private val _externalCredentialSaveResponse = MutableLiveData<LiveDataEventWithContent<Serializable>>()

    val externalCredentialResultDetails: LiveData<ExternalCredentialValidation?>
        get() = _externalCredentialResult
    private val _externalCredentialResult = MutableLiveData<ExternalCredentialValidation?>()

    val recaptureEvent: LiveData<LiveDataEvent>
        get() = _recaptureEvent
    private val _recaptureEvent = MutableLiveData<LiveDataEvent>()

    fun setConfig(params: ExternalCredentialParams) {
        subjectId = params.subjectId
        flowType = params.flowType
    }

    fun validateExternalCredential(credentialId: String) = viewModelScope.launch {
        val savedSubjectId = searchSubjectId(credentialId)
        val subjectId = if (flowType == FlowType.ENROL) subjectId else savedSubjectId
        val result = if (credentialId.isEmpty()) {
            ExternalCredentialResult.CREDENTIAL_EMPTY
        } else {
            when (flowType) {
                FlowType.ENROL -> when (savedSubjectId) {
                    null -> ExternalCredentialResult.ENROL_OK
                    else -> ExternalCredentialResult.ENROL_DUPLICATE_FOUND
                }

                else -> when (savedSubjectId) {
                    null -> ExternalCredentialResult.SEARCH_NOT_FOUND
                    else -> ExternalCredentialResult.SEARCH_FOUND
                }
            }
        }
        _externalCredentialResult.value =
            ExternalCredentialValidation(ExternalCredential(subjectId = subjectId, data = credentialId), result)
    }

    fun confirmAndFinishFlow(credential: ExternalCredential) = viewModelScope.launch {
        try {
            val response = when (flowType) {
                FlowType.ENROL -> {
                    externalCredentialRepository.save(credential)
                    ExternalCredentialSaveResponse(subjectId = credential.subjectId!!, externalCredential = credential.data)
                }

                else -> {
                    ExternalCredentialSearchResponse(subjectId = credential.subjectId, externalCredential = credential.data)
                }
            }
            _externalCredentialSaveResponse.send(response)
        } catch (e: Exception) {
            Simber.e("Unable to finish external credential [$flowType] flow", e)
        }
    }

    fun recapture() {
        _externalCredentialResult.value = null
        _recaptureEvent.send()
    }

    fun skipScanning() {
        _externalCredentialSaveResponse.send(ExternalCredentialSkipResponse())
    }

    private suspend fun searchSubjectId(data: String): String? {
        return externalCredentialRepository.findByCredential(credential = data)?.subjectId
    }

}
