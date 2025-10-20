package com.simprints.feature.externalcredential.screens.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.search.model.toExternalCredential
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.orEmpty

@HiltViewModel
internal class ExternalCredentialViewModel @Inject internal constructor(
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
    private val eventRepository: SessionEventRepository,
) : ViewModel() {
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

    val externalCredentialTypes: LiveData<List<ExternalCredentialType>>
        get() = _externalCredentialTypes
    private val _externalCredentialTypes = MutableLiveData<List<ExternalCredentialType>>()

    init {
        viewModelScope.launch {
            val config = configManager.getProjectConfiguration()
            val allowedExternalCredentials = config.multifactorId?.allowedExternalCredentials.orEmpty()
            _externalCredentialTypes.postValue(allowedExternalCredentials)
        }
    }

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

    fun finish(result: ExternalCredentialSearchResult) {
        viewModelScope.launch {
            result.scannedCredential?.let { scannedCredential ->
                Simber.d("Saving External Credential Events for $scannedCredential")
                val credential = scannedCredential.toExternalCredential(params.subjectId.orEmpty())
                eventRepository.addOrUpdateEvent(
                    ExternalCredentialCaptureValueEvent(
                        createdAt = timeHelper.now(),
                        payloadId = scannedCredential.credentialScanId,
                        credential = credential,
                    ),
                )
                // TODO Add valid capture event data
                eventRepository.addOrUpdateEvent(
                    ExternalCredentialCaptureEvent(
                        startTime = timeHelper.now(),
                        endTime = timeHelper.now(),
                        payloadId = scannedCredential.credentialScanId,
                        autoCaptureStartTime = timeHelper.now(),
                        autoCaptureEndTime = timeHelper.now(),
                        ocrErrorCount = 0,
                        capturedTextLength = 0,
                        credentialTextLength = 0,
                        selectionId = credential.id,
                    ),
                )
            }
            _finishEvent.send(result)
        }
    }
}
