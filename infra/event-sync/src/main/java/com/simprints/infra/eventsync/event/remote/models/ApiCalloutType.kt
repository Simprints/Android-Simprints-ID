package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal enum class ApiCalloutType {
    Confirmation,
    Enrolment,
    EnrolmentLastBiometrics,
    Identification,
    Verification,
}
