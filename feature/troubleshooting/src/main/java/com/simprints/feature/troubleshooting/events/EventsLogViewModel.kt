package com.simprints.feature.troubleshooting.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.troubleshooting.IsoDateTimeFormatter
import com.simprints.feature.troubleshooting.adapter.TroubleshootingItemViewData
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
internal class EventsLogViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    @IsoDateTimeFormatter private val dateFormatter: SimpleDateFormat,
) : ViewModel() {
    private val _events = MutableLiveData<List<TroubleshootingItemViewData>>(emptyList())
    val events: LiveData<List<TroubleshootingItemViewData>>
        get() = _events

    fun collectEvents(scopeId: String) {
        viewModelScope.launch {
            eventRepository
                .getEventsFromScope(scopeId)
                .map { event -> formatEventViewData(event) }
                .reversed()
                .ifEmpty { listOf(TroubleshootingItemViewData(title = "No events found (might be already up-synced)")) }
                .let { _events.postValue(it) }
        }
    }

    private fun formatEventViewData(event: Event): TroubleshootingItemViewData = TroubleshootingItemViewData(
        title = event.type.name,
        subtitle = formatTimestampSubtitle(
            event.payload.createdAt.ms,
            event.payload.endedAt?.ms,
        ),
        body = "ID: ${event.id}\n" + event.payload.toSafeString(),
    )

    private fun formatTimestampSubtitle(
        startMs: Long,
        endMs: Long? = null,
    ): String = "Started: ${dateFormatter.format(startMs)}" +
        endMs?.let { "\nEnded: ${dateFormatter.format(it)}" }.orEmpty()
}
