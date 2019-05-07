package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiEnrolmentCallback

@Keep
class EnrolmentCallback(val guid: String): Callback(CallbackType.ENROLMENT)

fun EnrolmentCallback.toApiEnrolmentCallback() = ApiEnrolmentCallback(guid)
