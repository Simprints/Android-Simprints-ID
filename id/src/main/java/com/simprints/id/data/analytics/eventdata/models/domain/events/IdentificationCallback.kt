package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiIdentificationCallback

@Keep
class IdentificationCallback(val sessionId: String, val scores: List<CallbackComparisonScore>): Callback(CallbackType.IDENTIFICATION)

fun IdentificationCallback.toApiIentificationCallback() =
    ApiIdentificationCallback(sessionId, scores.map { it.toApiCallbackComparisonScore() })
