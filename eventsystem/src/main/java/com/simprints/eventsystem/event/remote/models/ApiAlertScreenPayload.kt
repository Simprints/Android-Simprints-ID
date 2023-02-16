package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.eventsystem.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType
import com.simprints.eventsystem.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.*
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.AlertScreen


@Keep
data class ApiAlertScreenPayload(override val startTime: Long,
                                 override val version: Int,
                                 val alertType: ApiAlertScreenEventType) : ApiEventPayload(AlertScreen, version, startTime) {

    constructor(domainPayload: AlertScreenPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.alertType.fromDomainToApi())

    @Keep
    enum class ApiAlertScreenEventType {
        DIFFERENT_PROJECT_ID,
        DIFFERENT_USER_ID,
        GUID_NOT_FOUND_ONLINE,
        GUID_NOT_FOUND_OFFLINE,
        BLUETOOTH_NOT_SUPPORTED,
        LOW_BATTERY,

        UNEXPECTED_ERROR,
        DISCONNECTED,

        @Deprecated("Fingerprint module doesn't triggers it anymore")
        MULTIPLE_PAIRED_SCANNERS,

        @Deprecated("Fingerprint module doesn't triggers it anymore")
        NOT_PAIRED,
        BLUETOOTH_NOT_ENABLED,
        NFC_NOT_ENABLED,
        NFC_PAIR,
        SERIAL_ENTRY_PAIR,
        OTA,
        OTA_RECOVERY,
        OTA_FAILED,

        @Deprecated("That can never been triggered, so to be removed soon")
        INVALID_INTENT_ACTION,
        ENROLMENT_LAST_BIOMETRICS_FAILED,
        INVALID_STATE_FOR_INTENT_ACTION,
        INVALID_METADATA,
        INVALID_MODULE_ID,
        INVALID_PROJECT_ID,
        INVALID_SELECTED_ID,
        INVALID_SESSION_ID,
        INVALID_USER_ID,
        INVALID_VERIFY_ID,
        INTEGRITY_SERVICE_ERROR,
        FACE_LICENSE_INVALID,
        BACKEND_MAINTENANCE_ERROR,
        FACE_LICENSE_MISSING,
        GOOGLE_PLAY_SERVICES_OUTDATED,
        MISSING_GOOGLE_PLAY_SERVICES,
        MissingOrOutdatedGooglePlayStoreApp
    }
}


fun AlertScreenEventType.fromDomainToApi(): ApiAlertScreenEventType =
    when (this) {
        AlertScreenEventType.DIFFERENT_PROJECT_ID -> DIFFERENT_PROJECT_ID
        AlertScreenEventType.DIFFERENT_USER_ID -> DIFFERENT_USER_ID
        AlertScreenEventType.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
        AlertScreenEventType.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
        AlertScreenEventType.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
        AlertScreenEventType.LOW_BATTERY -> LOW_BATTERY
        AlertScreenEventType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
        AlertScreenEventType.DISCONNECTED -> DISCONNECTED
        AlertScreenEventType.MULTIPLE_PAIRED_SCANNERS -> MULTIPLE_PAIRED_SCANNERS
        AlertScreenEventType.NOT_PAIRED -> NOT_PAIRED
        AlertScreenEventType.BLUETOOTH_NOT_ENABLED -> BLUETOOTH_NOT_ENABLED
        AlertScreenEventType.NFC_NOT_ENABLED -> NFC_NOT_ENABLED
        AlertScreenEventType.NFC_PAIR -> NFC_PAIR
        AlertScreenEventType.SERIAL_ENTRY_PAIR -> SERIAL_ENTRY_PAIR
        AlertScreenEventType.OTA -> OTA
        AlertScreenEventType.OTA_RECOVERY -> OTA_RECOVERY
        AlertScreenEventType.OTA_FAILED -> OTA_FAILED
        AlertScreenEventType.INVALID_INTENT_ACTION -> INVALID_INTENT_ACTION
        AlertScreenEventType.INVALID_METADATA -> INVALID_METADATA
        AlertScreenEventType.INVALID_MODULE_ID -> INVALID_MODULE_ID
        AlertScreenEventType.INVALID_PROJECT_ID -> INVALID_PROJECT_ID
        AlertScreenEventType.INVALID_SELECTED_ID -> INVALID_SELECTED_ID
        AlertScreenEventType.INVALID_SESSION_ID -> INVALID_SESSION_ID
        AlertScreenEventType.INVALID_USER_ID -> INVALID_USER_ID
        AlertScreenEventType.INVALID_VERIFY_ID -> INVALID_VERIFY_ID
        AlertScreenEventType.INTEGRITY_SERVICE_ERROR -> INTEGRITY_SERVICE_ERROR
        AlertScreenEventType.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
        AlertScreenEventType.INVALID_STATE_FOR_INTENT_ACTION -> INVALID_STATE_FOR_INTENT_ACTION
        AlertScreenEventType.FACE_LICENSE_INVALID -> FACE_LICENSE_INVALID
        AlertScreenEventType.FACE_LICENSE_MISSING -> FACE_LICENSE_MISSING
        AlertScreenEventType.BACKEND_MAINTENANCE_ERROR -> BACKEND_MAINTENANCE_ERROR
        AlertScreenEventType.GOOGLE_PLAY_SERVICES_OUTDATED -> GOOGLE_PLAY_SERVICES_OUTDATED
        AlertScreenEventType.MISSING_GOOGLE_PLAY_SERVICES -> MISSING_GOOGLE_PLAY_SERVICES
        AlertScreenEventType.MissingOrOutdatedGooglePlayStoreApp ->MissingOrOutdatedGooglePlayStoreApp
    }
