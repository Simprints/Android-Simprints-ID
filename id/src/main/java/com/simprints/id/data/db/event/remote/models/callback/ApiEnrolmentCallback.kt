package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep

@Keep
class ApiEnrolmentCallback(val guid: String): ApiCallback(ApiCallbackType.ENROLMENT)
