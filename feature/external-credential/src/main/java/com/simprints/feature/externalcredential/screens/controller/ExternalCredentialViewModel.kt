package com.simprints.feature.externalcredential.screens.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.usecase.ExternalCredentialEventTrackerUseCase
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialViewModel @Inject internal constructor(
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
    private val saveExternalCaptureEvents: ExternalCredentialEventTrackerUseCase,
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

    private lateinit var selectionStartTime: Timestamp
    private lateinit var selectionEventId: String
    private lateinit var captureStartTime: Timestamp
    private var selectedSkipReason: ExternalCredentialSelectionEvent.SkipReason? = null
    private var selectedSkipOtherText: String? = null

    init {
        viewModelScope.launch {
            val config = configManager.getProjectConfiguration()
            val allowedExternalCredentials = config.multifactorId?.allowedExternalCredentials.orEmpty()
            _externalCredentialTypes.postValue(allowedExternalCredentials)
        }
    }

    fun selectionStarted() {
        selectionStartTime = timeHelper.now()
    }

    fun skipOptionSelected(skipOption: ExternalCredentialSelectionEvent.SkipReason) {
        selectedSkipReason = skipOption
    }

    fun skipOtherReasonChanged(otherText: String?) {
        selectedSkipOtherText = otherText?.ifBlank { null }
    }

    private fun updateState(state: (ExternalCredentialState) -> ExternalCredentialState) {
        this.state = state(this.state)
    }

    fun setSelectedExternalCredentialType(selectedType: ExternalCredentialType?) {
        viewModelScope.launch {
            if (selectedType != null) {
                val selectionEndTime = timeHelper.now()
                selectionEventId = saveExternalCaptureEvents.saveSelectionEvent(selectionStartTime, selectionEndTime, selectedType)
                captureStartTime = timeHelper.now()
            }
            updateState { it.copy(selectedType = selectedType) }
        }
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
            if (result.scannedCredential == null) {
                selectedSkipReason?.let { reason ->
                    saveExternalCaptureEvents.saveSkippedEvent(selectionStartTime, reason, selectedSkipOtherText)
                }
            } else {
                saveExternalCaptureEvents.saveCaptureEvents(
                    captureStartTime,
                    params.subjectId.orEmpty(),
                    result.scannedCredential,
                    selectionEventId,
                )
            }
            _finishEvent.send(result)
        }
    }
}
