package com.simprints.id.data.db.event.remote.events.callback

import androidx.annotation.Keep

@Keep
class ApiIdentificationCallback(val sessionId: String,
                                val scores: List<ApiCallbackComparisonScore>): ApiCallback(ApiCallbackType.IDENTIFICATION)
