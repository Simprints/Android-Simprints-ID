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
import com.simprints.feature.externalcredential.model.ExternalCredentialResponse
import com.simprints.feature.externalcredential.model.ExternalCredentialResult
import com.simprints.feature.externalcredential.screens.select.LayoutConfig
import com.simprints.feature.externalcredential.screens.select.ExternalCredentialPreviewLayoutConfig
import com.simprints.infra.external.credential.store.model.ExternalCredential
import com.simprints.infra.external.credential.store.repository.ExternalCredentialRepository
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialViewModel @Inject constructor(
    private val externalCredentialRepository: ExternalCredentialRepository,
    val layoutRepository: LayoutRepository,
    val ocrLayoutRepository: OcrLayoutRepository,
    val qrLayoutRepository: QrLayoutRepository,
) : ViewModel() {

    var subjectId: String? = null
    private var flowType: FlowType? = null

    val externalCredentialSaveResponse: LiveData<LiveDataEventWithContent<Serializable>>
        get() = _externalCredentialSaveResponse
    private val _externalCredentialSaveResponse = MutableLiveData<LiveDataEventWithContent<Serializable>>()

    val externalCredentialResultDetails: LiveData<ExternalCredentialValidation?>
        get() = _externalCredentialResult
    private val _externalCredentialResult = MutableLiveData<ExternalCredentialValidation?>()

    val layoutConfigLiveData: LiveData<LayoutConfig>
        get() = _layoutConfigLiveData
    private val _layoutConfigLiveData = MutableLiveData<LayoutConfig>(layoutRepository.getConfig())

    val ocrLayoutConfigLiveData: LiveData<ExternalCredentialPreviewLayoutConfig>
        get() = _ocrLayoutConfigLiveData
    private val _ocrLayoutConfigLiveData = MutableLiveData(ocrLayoutRepository.getConfig())

    val qrLayoutConfigLiveData: LiveData<ExternalCredentialPreviewLayoutConfig>
        get() = _qrLayoutConfigLiveData
    private val _qrLayoutConfigLiveData = MutableLiveData(qrLayoutRepository.getConfig())

    val recaptureEvent: LiveData<LiveDataEvent>
        get() = _recaptureEvent
    private val _recaptureEvent = MutableLiveData<LiveDataEvent>()

    val exitFormEvent: LiveData<LiveDataEvent>
        get() = _exitFormEvent
    private val _exitFormEvent = MutableLiveData<LiveDataEvent>()

    init {
        layoutRepository.onConfigUpdated = { _layoutConfigLiveData.postValue(it) }
        ocrLayoutRepository.onConfigUpdated = { _ocrLayoutConfigLiveData.postValue(it) }
        qrLayoutRepository.onConfigUpdated = { _qrLayoutConfigLiveData.postValue(it) }
    }

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

    fun confirmAndFinishFlow(credential: ExternalCredential, imagePath: String?, addCredentialRightNow: Boolean = false) =
        viewModelScope.launch {
            try {
                val response = when (flowType) {
                    FlowType.ENROL -> {
                        if (addCredentialRightNow) {
                            externalCredentialRepository.save(credential)
                        }
                        ExternalCredentialResponse.ExternalCredentialSaveResponse(
                            subjectId = credential.subjectId!!,
                            externalCredential = credential.data,
                            imagePreviewPath = imagePath
                        )
                    }

                    else -> {
                        ExternalCredentialResponse.ExternalCredentialSearchResponse(
                            subjectId = credential.subjectId,
                            externalCredential = credential.data,
                            imagePreviewPath = imagePath
                        )
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
        _externalCredentialSaveResponse.send(ExternalCredentialResponse.ExternalCredentialSkipResponse())
    }

    private suspend fun searchSubjectId(data: String): String? {
        return externalCredentialRepository.findByCredential(credential = data)?.subjectId
    }

    fun handleBackButton() {
        _exitFormEvent.send()
    }

}
