package com.simprints.feature.alert.screen

import androidx.lifecycle.ViewModel
import com.simprints.core.DeviceID
import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ALERT
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
internal class AlertViewModel @Inject constructor(
    @param:DeviceID private val deviceId: String,
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    private val authStore: AuthStore,
    @param:SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ViewModel() {
    private lateinit var cachedAlertEvent: AlertScreenEvent

    fun saveAlertEvent(type: AlertScreenEvent.AlertScreenPayload.AlertScreenEventType) {
        sessionCoroutineScope.launch {
            val event = AlertScreenEvent(timeHelper.now(), type)
            Simber.setUserProperty(LoggingConstants.CrashReportingCustomKeys.ALERT_EVENT_ID, event.id)

            eventRepository.addOrUpdateEvent(event)
            // Preserving the alert event to be able to export its data if requested by user
            cachedAlertEvent = event

            // Report non-fatal to keep the logs for investigation.
            // Called after saving the event to have the event ID in user properties for easier correlation.
            if (type == AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.UNEXPECTED_ERROR) {
                Simber.w("Unexpected error alert screen displayed", UnexpectedErrorAlertScreenException(), tag = ALERT)
            }
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
