package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import io.realm.internal.Keep

@Keep
class ApiIdentificationCallback(val sessionId:String,
                                val scores: List<ApiCallbackComparisonScore>): ApiCallback(ApiCallbackType.IDENTIFICATION)

