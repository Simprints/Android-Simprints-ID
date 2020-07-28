package com.simprints.id.data.db.event.remote.models.callback

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
