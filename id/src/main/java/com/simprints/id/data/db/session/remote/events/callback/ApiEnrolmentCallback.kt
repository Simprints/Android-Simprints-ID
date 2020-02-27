package com.simprints.id.data.db.session.remote.events.callback

import androidx.annotation.Keep

@Keep
class ApiEnrolmentCallback(val guid: String): ApiCallback(ApiCallbackType.ENROLMENT)
