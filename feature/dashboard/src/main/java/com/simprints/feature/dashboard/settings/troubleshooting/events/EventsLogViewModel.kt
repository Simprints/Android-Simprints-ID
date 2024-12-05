package com.simprints.feature.dashboard.settings.troubleshooting.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.dashboard.settings.troubleshooting.adapter.TroubleshootingItemViewData
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.scope.EventScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
internal class EventsLogViewModel @Inject constructor(
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _scopes = MutableLiveData<List<TroubleshootingItemViewData>>(emptyList())
    val scopes: LiveData<List<TroubleshootingItemViewData>>
        get() = _scopes

    private val _events = MutableLiveData<List<TroubleshootingItemViewData>>(emptyList())
    val events: LiveData<List<TroubleshootingItemViewData>>
        get() = _events

    fun collectEventScopes() {
        viewModelScope.launch {
            eventRepository.getAllScopes()
                .map { scope -> formatScopeViewData(scope) }
                .ifEmpty { listOf(TroubleshootingItemViewData(title = "No event scopes found")) }
                .let { _scopes.postValue(it) }
        }
    }

    fun collectEvents(scopeId: String) {
        viewModelScope.launch {
            eventRepository.getEventsFromScope(scopeId)
                .map { event -> formatEventViewData(event) }
                .reversed()
                .ifEmpty { listOf(TroubleshootingItemViewData(title = "No events found")) }
                .let { _events.postValue(it) }
        }
    }

    private fun formatScopeViewData(scope: EventScope): TroubleshootingItemViewData =
        TroubleshootingItemViewData(
            title = scope.id,
            subtitle = formatTimestampSubtitle(scope.createdAt.ms, scope.endedAt?.ms),
            body = """
                    Type: ${scope.type} | End cause: ${scope.payload.endCause}
                    ${scope.payload.language} | ${scope.payload.sidVersion} | Lib: ${scope.payload.libSimprintsVersion}
                    Configuration ID: ${scope.payload.projectConfigurationId}
                    """.trimIndent(),
            navigationId = scope.id,
        )

    private fun formatEventViewData(event: Event): TroubleshootingItemViewData =
        TroubleshootingItemViewData(
            title = event.type.name,
            subtitle = formatTimestampSubtitle(
                event.payload.createdAt.ms,
                event.payload.endedAt?.ms
            ),
            body = "ID: ${event.id}\n" + event.payload.toSafeString(),
        )

    private fun formatTimestampSubtitle(startMs: Long, endMs: Long? = null): String =
        "Started: ${Date(startMs)}" + endMs?.let { "\nEnded: ${Date(it)}" }.orEmpty()

}
