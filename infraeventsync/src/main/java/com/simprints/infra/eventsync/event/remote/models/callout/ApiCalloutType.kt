package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep

@Keep
enum class ApiCalloutType {
    Confirmation,
    Enrolment,
    EnrolmentLastBiometrics,
    Identification,
    Verification
}
