package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import io.realm.internal.Keep

@Keep
class ApiEnrolmentCallback(val guid:String): ApiCallback(ApiCallbackType.ENROLMENT)
