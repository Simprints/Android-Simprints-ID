package com.simprints.id.data.analytics.eventdata.models.domain.events.callout

import androidx.annotation.Keep

@Keep
enum class CalloutType {
    CONFIRMATION,
    ENROLMENT,
    IDENTIFICATION,
    VERIFICATION
}
