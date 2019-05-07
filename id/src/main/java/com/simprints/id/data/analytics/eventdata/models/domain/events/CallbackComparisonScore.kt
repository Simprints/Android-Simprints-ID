package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiCallbackComparisonScore
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiTier
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier

@Keep
class CallbackComparisonScore(val guid: String, val confidence: Int, val tier: Tier)

fun CallbackComparisonScore.toApiCallbackComparisonScore() =
    ApiCallbackComparisonScore(guid, confidence, ApiTier.valueOf(tier.name))
