package com.simprints.feature.alert.screen

import androidx.lifecycle.ViewModel
import com.simprints.core.DeviceID
import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.events.session.SessionEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
internal class AlertViewModel @Inject constructor(
    @DeviceID private val deviceId: String,
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    private val authStore: AuthStore,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ViewModel() {
    private lateinit var cachedAlertEvent: AlertScreenEvent

    fun saveAlertEvent(type: AlertScreenEvent.AlertScreenPayload.AlertScreenEventType) {
        sessionCoroutineScope.launch {
            val event = AlertScreenEvent(timeHelper.now(), type)
            eventRepository.addOrUpdateEvent(event)
            // Preserving the alert event to be able to export its data if requested by user
            cachedAlertEvent = event
        }
    }

    fun collectExportData(): String = runBlocking {
        val sessionId = eventRepository.getCurrentSessionScope().id

        """
        Event ID:   ${cachedAlertEvent.id}
        Timestamp:  ${cachedAlertEvent.payload.createdAt.ms}
        Project ID: ${authStore.signedInProjectId}
        User ID:    ${authStore.signedInUserId?.value}
        Device ID:  $deviceId
        Session ID: $sessionId
        Alert type: ${cachedAlertEvent.payload.alertType}
        """.trimIndent()
    }
}
