package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiIdentificationCallback(val sessionId: String,
                                     val scores: List<ApiCallbackComparisonScore>) : ApiCallback(ApiCallbackType.Identification)
