package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.callback.CallbackComparisonScore
import com.simprints.infra.eventsync.event.remote.models.ApiConfidenceMatch
import com.simprints.infra.eventsync.event.remote.models.ApiTier

@Keep
internal sealed class ApiCallbackComparisonScore {
    @Keep
    data class ApiCallbackComparisonScoreV1(
        val guid: String,
        val confidence: Int,
        val tier: ApiTier,
    ) : ApiCallbackComparisonScore()

    @Keep
    data class ApiCallbackComparisonScoreV2(
        val guid: String,
        val confidence: Int,
        val confidenceMatch: ApiConfidenceMatch = ApiConfidenceMatch.NONE,
    ) : ApiCallbackComparisonScore()
}

internal fun CallbackComparisonScore.fromDomainToApi(version: Int) = when (version) {
    1 -> ApiCallbackComparisonScore.ApiCallbackComparisonScoreV1(
        guid,
        confidence,
        ApiTier.fromConfidence(confidenceMatch),
    )

    else -> ApiCallbackComparisonScore.ApiCallbackComparisonScoreV2(
        guid,
        confidence,
        ApiConfidenceMatch.valueOf(confidenceMatch.name),
    )
}
