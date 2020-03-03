package com.simprints.id.data.db.session.remote.events.callback

import androidx.annotation.Keep

@Keep
enum class ApiCallbackType {
    ENROLMENT,
    IDENTIFICATION,
    REFUSAL,
    VERIFICATION,
    ERROR,
    CONFIRMATION
}
