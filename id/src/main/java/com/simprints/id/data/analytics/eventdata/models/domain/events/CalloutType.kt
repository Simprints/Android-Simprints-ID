package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
enum class CalloutType {
    CONFIRMATION,
    ENROLMENT,
    IDENTIFICATION,
    VERIFICATION
}
