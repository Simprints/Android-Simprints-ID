package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ALERT_SCREEN
import java.util.*


@Keep
class AlertScreenEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: AlertScreenPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        alertType: AlertScreenEventType,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
        AlertScreenPayload(createdAt, DEFAULT_EVENT_VERSION, alertType),
        ALERT_SCREEN)

    @Keep
    class AlertScreenPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val alertType: AlertScreenEventType
    ) : EventPayload(ALERT_SCREEN, eventVersion, createdAt) {

        enum class AlertScreenEventType {
            DIFFERENT_PROJECT_ID,
            DIFFERENT_USER_ID,
            GUID_NOT_FOUND_ONLINE,
            GUID_NOT_FOUND_OFFLINE,
            BLUETOOTH_NOT_SUPPORTED,
            LOW_BATTERY,
            UNEXPECTED_ERROR,
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
            SAFETYNET_ERROR,
            ENROLMENT_LAST_BIOMETRICS_FAILED,
            INVALID_STATE_FOR_INTENT_ACTION
        }
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
