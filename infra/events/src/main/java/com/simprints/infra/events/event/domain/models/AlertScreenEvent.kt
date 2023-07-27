package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EventType.ALERT_SCREEN
import java.util.*


@Keep
data class AlertScreenEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: AlertScreenPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        alertType: AlertScreenPayload.AlertScreenEventType,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        AlertScreenPayload(createdAt, EVENT_VERSION, alertType),
        ALERT_SCREEN)

    @Keep
    data class AlertScreenPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val alertType: AlertScreenEventType,
        override val type: EventType = ALERT_SCREEN,
        override val endedAt: Long = 0
    ) : EventPayload() {

        enum class AlertScreenEventType {
            DIFFERENT_PROJECT_ID,
            DIFFERENT_USER_ID,
            GUID_NOT_FOUND_ONLINE,
            GUID_NOT_FOUND_OFFLINE,
            BLUETOOTH_NOT_SUPPORTED,
            BLUETOOTH_NO_PERMISSION,
            LOW_BATTERY,
            UNEXPECTED_ERROR,
            BACKEND_MAINTENANCE_ERROR,
            DISCONNECTED,
            MULTIPLE_PAIRED_SCANNERS,
            NOT_PAIRED,
            BLUETOOTH_NOT_ENABLED,
            NFC_NOT_ENABLED,
            NFC_PAIR,
            SERIAL_ENTRY_PAIR,
            OTA,
            OTA_RECOVERY,
            OTA_FAILED,
            INVALID_INTENT_ACTION,
            INVALID_METADATA,
            INVALID_MODULE_ID,
            INVALID_PROJECT_ID,
            INVALID_SELECTED_ID,
            INVALID_SESSION_ID,
            INVALID_USER_ID,
            INVALID_VERIFY_ID,
            INTEGRITY_SERVICE_ERROR,
            ENROLMENT_LAST_BIOMETRICS_FAILED,
            INVALID_STATE_FOR_INTENT_ACTION,
            FACE_LICENSE_INVALID,
            FACE_LICENSE_MISSING,
            GOOGLE_PLAY_SERVICES_OUTDATED,
            MISSING_GOOGLE_PLAY_SERVICES,
            MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
            PROJECT_PAUSED,
            PROJECT_ENDING
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
