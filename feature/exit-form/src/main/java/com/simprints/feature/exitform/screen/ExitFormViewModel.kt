package com.simprints.feature.exitform.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.ExternalScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.RefusalEvent
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExitFormViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope
) : ViewModel() {

    private val exitFormStart: Timestamp = timeHelper.now()

    private var selectedOption: ExitFormOption? = null
    private var providedReason: String? = null

    private val _requestReasonEvent = MutableLiveData<LiveDataEvent>()
    val requestReasonEvent: LiveData<LiveDataEvent>
        get() = _requestReasonEvent

    private val _requestFormSubmitEvent = MutableLiveData<LiveDataEvent>()
    val requestFormSubmitEvent: LiveData<LiveDataEvent>
        get() = _requestFormSubmitEvent

    private val _requestSelectOptionEvent = MutableLiveData<LiveDataEvent>()
    val requestSelectOptionEvent: LiveData<LiveDataEvent>
        get() = _requestSelectOptionEvent

    private val _submitEnabled = MutableLiveData(false)
    val submitEnabled: LiveData<Boolean>
        get() = _submitEnabled

    private val _finishEvent = MutableLiveData<LiveDataEventWithContent<Pair<ExitFormOption, String>>>()
    val finishEvent: LiveData<LiveDataEventWithContent<Pair<ExitFormOption, String>>>
        get() = _finishEvent

    fun optionSelected(option: ExitFormOption) {
        selectedOption = option
        Simber.i("Radio option ${option.logName} clicked")

        if (option.requiresInfo) {
            _requestReasonEvent.send()
        }
        _submitEnabled.postValue(canSubmit(selectedOption, providedReason))
    }

    fun reasonTextChanged(newReason: String?) {
        providedReason = newReason
        _submitEnabled.postValue(canSubmit(selectedOption, newReason))
    }

    private fun canSubmit(option: ExitFormOption?, reason: String?) =
        option != null && !(option.requiresInfo && reason.isNullOrBlank())

    fun handleBackButton() {
        if (selectedOption == null) {
            _requestSelectOptionEvent.send()
        } else {
            _requestFormSubmitEvent.send()
        }
    }

    fun submitClicked(reasonText: String?) {
        selectedOption?.let {
            if (canSubmit(it, reasonText)) {
                logRefusalEvent(it, reasonText.orEmpty())
                _finishEvent.send(it to reasonText.orEmpty())
            }
        }
    }

    private fun logRefusalEvent(option: ExitFormOption, reasonText: String) = externalScope.launch {
        eventRepository.addOrUpdateEvent(RefusalEvent(exitFormStart, timeHelper.now(), option.answer, reasonText))
    }
}
