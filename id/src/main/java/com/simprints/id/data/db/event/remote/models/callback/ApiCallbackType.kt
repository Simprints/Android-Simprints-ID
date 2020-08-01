package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep

@Keep
enum class ApiCallbackType {
    Enrolment,
    Identification,
    Refusal,
    Verification,
    Error,
    Confirmation
}
