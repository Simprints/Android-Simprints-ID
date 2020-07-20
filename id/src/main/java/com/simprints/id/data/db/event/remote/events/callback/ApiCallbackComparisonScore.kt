package com.simprints.id.data.db.event.remote.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.callback.CallbackComparisonScore
import com.simprints.id.data.db.event.remote.events.ApiTier

@Keep
class ApiCallbackComparisonScore(val guid: String, val confidence: Int, val tier: ApiTier)

fun CallbackComparisonScore.fromDomainToApi() =
    ApiCallbackComparisonScore(guid, confidence, ApiTier.valueOf(tier.name))
