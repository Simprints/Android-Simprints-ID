package com.simprints.infra.events.remote.models.callback

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
