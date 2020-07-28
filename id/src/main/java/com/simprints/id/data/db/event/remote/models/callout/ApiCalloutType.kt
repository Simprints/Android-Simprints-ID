package com.simprints.id.data.db.event.remote.models.callout

import io.realm.internal.Keep

@Keep
enum class ApiCalloutType {
    CONFIRMATION,
    ENROLMENT,
    ENROLMENT_LAST_BIOMETRICS,
    IDENTIFICATION,
    VERIFICATION
}
