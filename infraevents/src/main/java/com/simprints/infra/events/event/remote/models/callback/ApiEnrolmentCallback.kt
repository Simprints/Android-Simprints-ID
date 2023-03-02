package com.simprints.infra.events.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiEnrolmentCallback(val guid: String): ApiCallback(ApiCallbackType.Enrolment)
