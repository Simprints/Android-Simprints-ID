package com.simprints.eventsystem.event.remote.models.callout

import androidx.annotation.Keep

@Keep
enum class ApiCalloutType {
    Confirmation,
    Enrolment,
    EnrolmentLastBiometrics,
    Identification,
    Verification
}
