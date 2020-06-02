package com.simprints.id.data.db.session.remote.events.callout

import io.realm.internal.Keep

@Keep
enum class ApiCalloutType {
    CONFIRMATION,
    ENROLMENT,
    ENROLMENT_LAST_BIOMETRICS,
    IDENTIFICATION,
    VERIFICATION
}
