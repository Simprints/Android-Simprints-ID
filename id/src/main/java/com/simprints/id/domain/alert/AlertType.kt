package com.simprints.id.domain.alert

import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent.AlertScreenEventType
import kotlin.reflect.jvm.internal.impl.descriptors.Modality

enum class AlertType(val modalities: List<Modality>? = null) {
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE,
    ENROLMENT_LAST_BIOMETRICS_FAILED,
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    OFFLINE_DURING_SETUP,
    SETUP_MODALITY_DOWNLOAD_CANCELLED,
    SAFETYNET_ERROR,
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
    AlertType.OFFLINE_DURING_SETUP -> AlertScreenEventType.GUID_NOT_FOUND_OFFLINE
    AlertType.SETUP_MODALITY_DOWNLOAD_CANCELLED -> AlertScreenEventType.GUID_NOT_FOUND_OFFLINE
}
