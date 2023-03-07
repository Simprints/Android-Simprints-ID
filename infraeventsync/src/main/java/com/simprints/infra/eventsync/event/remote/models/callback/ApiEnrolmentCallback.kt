package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep
import com.simprints.infra.eventsync.event.remote.models.callback.ApiCallback
import com.simprints.infra.eventsync.event.remote.models.callback.ApiCallbackType

@Keep
data class ApiEnrolmentCallback(val guid: String): ApiCallback(ApiCallbackType.Enrolment)
