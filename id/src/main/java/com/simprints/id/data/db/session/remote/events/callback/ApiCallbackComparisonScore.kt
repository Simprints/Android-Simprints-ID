package com.simprints.id.data.db.session.remote.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.events.callback.CallbackComparisonScore
import com.simprints.id.data.db.session.remote.events.ApiTier

@Keep
class ApiCallbackComparisonScore(val guid: String, val confidence: Int, val tier: ApiTier)

fun CallbackComparisonScore.fromDomainToApi() =
    ApiCallbackComparisonScore(guid, confidence, ApiTier.valueOf(tier.name))
