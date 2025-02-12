package com.simprints.feature.exitform.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.SessionCoroutineScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.exitform.ExitFormOption
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.RefusalEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExitFormViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ViewModel() {
    private val exitFormStart: Timestamp = timeHelper.now()

    private val _visibleOptions: MutableLiveData<Set<ExitFormOption>> = MutableLiveData(DEFAULT_OPTIONS)
    val visibleOptions: LiveData<Set<ExitFormOption>>
        get() = _visibleOptions

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

    fun start() {
        viewModelScope.launch {
            val projectConfig = configManager.getProjectConfiguration()
            if (projectConfig.general.modalities.contains(GeneralConfiguration.Modality.FINGERPRINT)) {
                val options = DEFAULT_OPTIONS.toMutableSet()
                options.remove(ExitFormOption.AppNotWorking)
                options.add(ExitFormOption.ScannerNotWorking)
                _visibleOptions.postValue(options)
            }
        }
    }

    private fun canSubmit(
        option: ExitFormOption?,
        reason: String?,
    ) = option != null && !(option.requiresInfo && reason.isNullOrBlank())

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

    private fun logRefusalEvent(
        option: ExitFormOption,
        reasonText: String,
    ) = sessionCoroutineScope.launch {
        eventRepository.addOrUpdateEvent(RefusalEvent(exitFormStart, timeHelper.now(), option.answer, reasonText))
    }

    companion object {
        val DEFAULT_OPTIONS = setOf(
            ExitFormOption.ReligiousConcerns,
            ExitFormOption.DataConcerns,
            ExitFormOption.NoPermission,
            ExitFormOption.AppNotWorking,
            ExitFormOption.PersonNotPresent,
            ExitFormOption.TooYoung,
            ExitFormOption.WrongAgeGroupSelected,
            ExitFormOption.UncooperativeChild,
            ExitFormOption.Other,
        )
    }
}
