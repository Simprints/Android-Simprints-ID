package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep
import com.simprints.infra.eventsync.event.remote.models.callback.ApiCallback
import com.simprints.infra.eventsync.event.remote.models.callback.ApiCallbackComparisonScore
import com.simprints.infra.eventsync.event.remote.models.callback.ApiCallbackType

@Keep
data class ApiIdentificationCallback(
    val sessionId: String,
    val scores: List<ApiCallbackComparisonScore>,
) : ApiCallback(ApiCallbackType.Identification)
