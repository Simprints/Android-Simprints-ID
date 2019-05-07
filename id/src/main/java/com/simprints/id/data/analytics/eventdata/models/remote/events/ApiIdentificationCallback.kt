package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep

@Keep
class ApiIdentificationCallback(val sessionId: String, scores: List<ApiCallbackComparisonScore>): ApiCallback(ApiCallbackType.IDENTIFICATION)
