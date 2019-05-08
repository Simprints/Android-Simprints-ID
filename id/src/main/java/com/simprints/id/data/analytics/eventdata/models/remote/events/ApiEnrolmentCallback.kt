package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep

@Keep
class ApiEnrolmentCallback(val guid: String): ApiCallback(ApiCallbackType.ENROLMENT)
