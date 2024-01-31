package com.simprints.feature.alert.screen

import androidx.lifecycle.ViewModel
import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AlertViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: EventRepository,
    @ExternalScope private val ioScope: CoroutineScope,
) : ViewModel() {

    fun saveAlertEvent(type: AlertScreenEvent.AlertScreenPayload.AlertScreenEventType) {
        ioScope.launch {
            eventRepository.addOrUpdateEvent(AlertScreenEvent(timeHelper.nowTimestamp(), type))
        }
    }
}
