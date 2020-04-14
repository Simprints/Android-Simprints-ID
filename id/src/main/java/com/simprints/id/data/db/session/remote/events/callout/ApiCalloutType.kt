package com.simprints.id.data.db.session.remote.events.callout

import io.realm.internal.Keep

@Keep
enum class ApiCalloutType {
    CONFIRMATION,
    ENROLMENT,
    IDENTIFICATION,
    VERIFICATION
}
