package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep

@Keep
internal enum class ApiCallbackType {
    Enrolment,
    Identification,
    Refusal,
    Verification,
    Error,
    Confirmation,
}
