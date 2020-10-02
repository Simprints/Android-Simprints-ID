package com.simprints.id.data.db.event.remote.models.callout

import io.realm.internal.Keep

@Keep
enum class ApiCalloutType {
    Confirmation,
    Enrolment,
    EnrolmentLastBiometrics,
    Identification,
    Verification
}
