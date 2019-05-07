package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep

@Keep
class ApiIdentificationCallback(val sessionId: String, val scores: List<ApiCallbackComparisonScore>): ApiCallback(ApiCallbackType.IDENTIFICATION)
