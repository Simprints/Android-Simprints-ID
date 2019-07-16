package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import androidx.annotation.Keep

@Keep
enum class ApiCallbackType {
    ENROLMENT,
    IDENTIFICATION,
    REFUSAL,
    VERIFICATION,
    ERROR,
    IDENTIFICATION_OUTCOME
}
