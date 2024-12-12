package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.BACKEND_MAINTENANCE_ERROR
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.BLUETOOTH_NOT_SUPPORTED
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.BLUETOOTH_NO_PERMISSION
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.DIFFERENT_PROJECT_ID
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.DIFFERENT_USER_ID
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.DISCONNECTED
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.ENROLMENT_LAST_BIOMETRICS_FAILED
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.GOOGLE_PLAY_SERVICES_OUTDATED
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.GUID_NOT_FOUND_OFFLINE
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.GUID_NOT_FOUND_ONLINE
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INTEGRITY_SERVICE_ERROR
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INVALID_INTENT_ACTION
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INVALID_METADATA
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INVALID_MODULE_ID
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INVALID_PROJECT_ID
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INVALID_SELECTED_ID
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INVALID_SESSION_ID
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INVALID_STATE_FOR_INTENT_ACTION
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INVALID_USER_ID
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.INVALID_VERIFY_ID
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.LICENSE_INVALID
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.LICENSE_MISSING
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.LOW_BATTERY
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.MISSING_GOOGLE_PLAY_SERVICES
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.MULTIPLE_PAIRED_SCANNERS
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.NFC_NOT_ENABLED
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.NFC_PAIR
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.NOT_PAIRED
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.OTA
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.OTA_FAILED
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.OTA_RECOVERY
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.PROJECT_ENDING
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.PROJECT_PAUSED
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.SERIAL_ENTRY_PAIR
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.UNEXPECTED_ERROR

@Keep
internal data class ApiAlertScreenPayload(
    override val startTime: ApiTimestamp,
    val alertType: ApiAlertScreenEventType,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: AlertScreenPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.alertType.fromDomainToApi(),
    )

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
        LICENSE_INVALID,
        BACKEND_MAINTENANCE_ERROR,
        LICENSE_MISSING,
        GOOGLE_PLAY_SERVICES_OUTDATED,
        MISSING_GOOGLE_PLAY_SERVICES,
        PROJECT_PAUSED,
        PROJECT_ENDING,
        MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
        BLUETOOTH_NO_PERMISSION,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal fun AlertScreenEventType.fromDomainToApi(): ApiAlertScreenEventType = when (this) {
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
    AlertScreenEventType.LICENSE_INVALID -> LICENSE_INVALID
    AlertScreenEventType.LICENSE_MISSING -> LICENSE_MISSING
    AlertScreenEventType.BACKEND_MAINTENANCE_ERROR -> BACKEND_MAINTENANCE_ERROR
    AlertScreenEventType.GOOGLE_PLAY_SERVICES_OUTDATED -> GOOGLE_PLAY_SERVICES_OUTDATED
    AlertScreenEventType.MISSING_GOOGLE_PLAY_SERVICES -> MISSING_GOOGLE_PLAY_SERVICES
    AlertScreenEventType.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP -> MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP
    AlertScreenEventType.PROJECT_PAUSED -> PROJECT_PAUSED
    AlertScreenEventType.PROJECT_ENDING -> PROJECT_ENDING
    AlertScreenEventType.BLUETOOTH_NO_PERMISSION -> BLUETOOTH_NO_PERMISSION
}
