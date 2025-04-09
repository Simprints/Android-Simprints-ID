package com.simprints.feature.externalcredential.screens.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.common.FlowType
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.externalcredential.model.ExternalCredentialConfirmationResult
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.model.ExternalCredentialResult
import com.simprints.feature.externalcredential.model.ExternalCredentialSaveResponse
import com.simprints.feature.externalcredential.model.ExternalCredentialSearchResponse
import com.simprints.feature.externalcredential.model.ExternalCredentialSkipResponse
import com.simprints.infra.external.credential.store.model.ExternalCredential
import com.simprints.infra.external.credential.store.repository.ExternalCredentialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialViewModel @Inject constructor(
    private val externalCredentialRepository: ExternalCredentialRepository<ExternalCredential.QrCode>
) : ViewModel() {

    private var subjectId: String? = null
    private var flowType: FlowType? = null

    val externalCredentialSaveResponse: LiveData<LiveDataEventWithContent<Serializable>>
        get() = _externalCredentialSaveResponse
    private val _externalCredentialSaveResponse = MutableLiveData<LiveDataEventWithContent<Serializable>>()

    val externalCredentialResult: LiveData<ExternalCredentialConfirmationResult?>
        get() = _externalCredentialResult
    private val _externalCredentialResult = MutableLiveData<ExternalCredentialConfirmationResult?>()

    val recaptureEvent: LiveData<LiveDataEvent>
        get() = _recaptureEvent
    private val _recaptureEvent = MutableLiveData<LiveDataEvent>()

    fun setConfig(params: ExternalCredentialParams) {
        subjectId = params.subjectId
        flowType = params.flowType
    }

    fun processExternalCredential(data: String) = viewModelScope.launch {
        val result: ExternalCredentialConfirmationResult = when (flowType) {
            // When enrolling new subject, it is necessary to link the external credential with subjectId, therefore, saving it.
            FlowType.ENROL -> {
                // TODO [MS-964] Probably move 'saveCredential()' to use case. Repo throws exception when the QR code is already saved
                val credential = runCatching { saveCredential(data) }.getOrNull()
                val result = when (credential) {
                    null -> ExternalCredentialResult.ENROL_DUPLICATE_FOUND
                    else -> ExternalCredentialResult.ENROL_OK
                }
                ExternalCredentialConfirmationResult(ExternalCredential.QrCode(credential?.subjectId, data), result)
            }

            // During any other flow type, it is necessary to find the subject ID linked to the external credential
            else -> {
                val subjectId = searchSubjectId(data)
                val result = when (subjectId) {
                    null -> ExternalCredentialResult.SEARCH_NOT_FOUND
                    else -> ExternalCredentialResult.SEARCH_FOUND
                }
                ExternalCredentialConfirmationResult(ExternalCredential.QrCode(subjectId, data), result)
            }
        }
        _externalCredentialResult.value = result
    }

    fun confirmAndFinishFlow(credential: ExternalCredential.QrCode) = viewModelScope.launch {
        val response = when (flowType) {
            FlowType.ENROL -> ExternalCredentialSaveResponse(subjectId = credential.subjectId!!, externalCredential = credential.data)
            else -> ExternalCredentialSearchResponse(subjectId = credential.subjectId, externalCredential = credential.data)
        }
        _externalCredentialSaveResponse.send(response)
    }

    fun recapture() {
        _externalCredentialResult.value = null
        _recaptureEvent.send()
    }

    fun skipScanning() {
        _externalCredentialSaveResponse.send(ExternalCredentialSkipResponse())
    }

    private suspend fun saveCredential(data: String): ExternalCredential.QrCode {
        val result = ExternalCredential.QrCode(
            data = data,
            subjectId = subjectId!!
        )
        externalCredentialRepository.save(
            credential = result
        )
        return result
    }

    private suspend fun searchSubjectId(data: String): String? {
        return externalCredentialRepository.findByCredential(
            credential = ExternalCredential.QrCode(
                data = data,
                subjectId = subjectId
            )
        )?.subjectId
    }

}
