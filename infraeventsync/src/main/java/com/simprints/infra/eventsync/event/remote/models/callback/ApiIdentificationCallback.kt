package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep

@Keep
internal data class ApiIdentificationCallback(
    val sessionId: String,
    val scores: List<ApiCallbackComparisonScore>,
) : ApiCallback(ApiCallbackType.Identification)
