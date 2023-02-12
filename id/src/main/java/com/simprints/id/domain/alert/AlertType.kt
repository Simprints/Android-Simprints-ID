package com.simprints.id.domain.alert

import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import kotlin.reflect.jvm.internal.impl.descriptors.Modality

enum class AlertType(val modalities: List<Modality>? = null) {
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE,
    ENROLMENT_LAST_BIOMETRICS_FAILED,
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    SAFETYNET_ERROR,
    GOOGLE_PLAY_SERVICES_OUTDATED,
    MISSING_GOOGLE_PLAY_SERVICES,
    UNEXPECTED_ERROR
}

fun AlertType.fromAlertToAlertTypeEvent() = when (this) {
    AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN -> AlertScreenEventType.DIFFERENT_PROJECT_ID
    AlertType.DIFFERENT_USER_ID_SIGNED_IN -> AlertScreenEventType.DIFFERENT_USER_ID
    AlertType.UNEXPECTED_ERROR -> AlertScreenEventType.UNEXPECTED_ERROR
    AlertType.SAFETYNET_ERROR -> AlertScreenEventType.SAFETYNET_ERROR
    AlertType.GUID_NOT_FOUND_ONLINE -> AlertScreenEventType.GUID_NOT_FOUND_ONLINE
    AlertType.GUID_NOT_FOUND_OFFLINE -> AlertScreenEventType.GUID_NOT_FOUND_OFFLINE
    AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED -> AlertScreenEventType.ENROLMENT_LAST_BIOMETRICS_FAILED
    AlertType.GOOGLE_PLAY_SERVICES_OUTDATED -> AlertScreenEventType.GOOGLE_PLAY_SERVICES_OUTDATED
    AlertType.MISSING_GOOGLE_PLAY_SERVICES -> AlertScreenEventType.MISSING_GOOGLE_PLAY_SERVICES
}
