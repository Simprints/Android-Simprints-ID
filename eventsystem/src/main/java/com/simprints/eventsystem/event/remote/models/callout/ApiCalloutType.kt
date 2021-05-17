package com.simprints.eventsystem.event.remote.models.callout

import io.realm.internal.Keep

@Keep
enum class ApiCalloutType {
    Confirmation,
    Enrolment,
    EnrolmentLastBiometrics,
    Identification,
    Verification
}
