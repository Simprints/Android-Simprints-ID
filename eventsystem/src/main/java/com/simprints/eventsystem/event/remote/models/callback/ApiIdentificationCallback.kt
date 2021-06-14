package com.simprints.eventsystem.event.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiIdentificationCallback(val sessionId: String,
                                     val scores: List<ApiCallbackComparisonScore>) : ApiCallback(ApiCallbackType.Identification)
