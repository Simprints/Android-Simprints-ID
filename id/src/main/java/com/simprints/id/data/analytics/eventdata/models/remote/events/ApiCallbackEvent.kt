package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.remote.events.callout.ApiCallout

@Keep
class ApiCallbackEvent(val relativeStartTime: Long, val callback: ApiCallback) : ApiEvent(ApiEventType.CALLBACK) {

    constructor(callbackEvent: CallbackEvent): this(callbackEvent.relativeStartTime, getApiCallback(callbackEvent.callback))
}

fun getApiCallback(callback: Callback): ApiCallback = when(callback) {
    is EnrolmentCallback -> callback.toApiEnrolmentCallback()
    is IdentificationCallback -> callback.toApiIentificationCallback()
    is VerificationCallback -> callback.toApiVerificationCallback()
    is RefusalCallback -> callback.toApiRefusalCallback()
    else -> throw Exception() //STOPSHIP
}
