package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiEnrolmentCallback(val guid: String): ApiCallback(ApiCallbackType.Enrolment)
