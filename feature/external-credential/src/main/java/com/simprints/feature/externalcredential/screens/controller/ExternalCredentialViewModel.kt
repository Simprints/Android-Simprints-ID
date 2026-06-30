package com.simprints.feature.externalcredential.screens.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
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
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialViewModel @Inject internal constructor(
    private val timeHelper: TimeHelper,
    private val configRepository: ConfigRepository,
    private val eventsTracker: ExternalCredentialEventTrackerUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var isInitialized = false
    lateinit var params: ExternalCredentialParams
        private set
    var defaultSkipReason: String? = null
        private set
    var skipReasonsHideHasNumber: Boolean = false
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
    var selectedSkipReason: ExternalCredentialSelectionEvent.SkipReason? = null
        private set
    private var selectedSkipOtherText: String? = null

    init {
        savedStateHandle.get<Timestamp>(KEY_SELECTION_START_TIME)?.let { selectionStartTime = it }
        savedStateHandle.get<String>(KEY_SELECTION_EVENT_ID)?.let { selectionEventId = it }
        savedStateHandle.get<Timestamp>(KEY_CAPTURE_START_TIME)?.let { captureStartTime = it }

        viewModelScope.launch {
            val config = configRepository.getProjectConfiguration()
            val experimental = config.experimental()
            val allowedExternalCredentials = config.multifactorId?.allowedExternalCredentials.orEmpty()
            _externalCredentialTypes.postValue(allowedExternalCredentials)
            defaultSkipReason = experimental.mfidDefaultSkipReason
            skipReasonsHideHasNumber = experimental.mfidSkipReasonsHideHasNumber
        }
    }

    fun selectionStarted() {
        if (savedStateHandle.contains(KEY_SELECTION_START_TIME)) {
            // Selection time has been recorded, which means the user has already started selection before process death.
            return
        }

        selectionStartTime = timeHelper.now()
        savedStateHandle[KEY_SELECTION_START_TIME] = selectionStartTime
    }

    fun skipOptionSelected(skipOption: ExternalCredentialSelectionEvent.SkipReason) {
        selectedSkipReason = skipOption
    }

    fun skipOtherReasonChanged(otherText: String?) {
        selectedSkipOtherText = otherText?.ifBlank { null }
    }

    fun bypassSkipScreen() {
        selectedSkipOtherText = defaultSkipReason
        finish(
            ExternalCredentialSearchResult.Skipped(
                flowType = params.flowType,
                skipReason = ExternalCredentialSelectionEvent.SkipReason.OTHER,
            ),
        )
    }

    private fun updateState(state: (ExternalCredentialState) -> ExternalCredentialState) {
        this.state = state(this.state)
    }

    fun setSelectedExternalCredentialType(selectedType: ExternalCredentialType?) {
        viewModelScope.launch {
            if (selectedType != null) {
                val selectionEndTime = timeHelper.now()
                selectionEventId = eventsTracker.saveSelectionEvent(selectionStartTime, selectionEndTime, selectedType)
                savedStateHandle[KEY_SELECTION_EVENT_ID] = selectionEventId
                captureStartTime = timeHelper.now()
                savedStateHandle[KEY_CAPTURE_START_TIME] = captureStartTime
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
            when (result) {
                is ExternalCredentialSearchResult.Complete -> {
                    eventsTracker.saveCaptureEvents(
                        credentialSearchResult = result,
                        subjectId = params.subjectId.orEmpty(),
                        startTime = captureStartTime,
                        selectionEventId = selectionEventId,
                    )
                }
                is ExternalCredentialSearchResult.Skipped -> {
                    eventsTracker.saveSkippedEvent(
                        startTime = selectionStartTime,
                        skipReason = result.skipReason,
                        skipOther = selectedSkipOtherText,
                    )
                }
            }
            _finishEvent.send(result)
        }
    }

    internal companion object {
        internal const val KEY_SELECTION_START_TIME = "selection_start_time"
        internal const val KEY_SELECTION_EVENT_ID = "selection_event_id"
        internal const val KEY_CAPTURE_START_TIME = "capture_start_time"
    }
}
