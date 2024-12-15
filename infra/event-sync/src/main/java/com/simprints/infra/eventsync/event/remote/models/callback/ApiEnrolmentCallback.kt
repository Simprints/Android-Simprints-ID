package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep

@Keep
internal data class ApiEnrolmentCallback(
    val guid: String,
) : ApiCallback(ApiCallbackType.Enrolment)
