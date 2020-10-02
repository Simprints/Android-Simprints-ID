package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.callback.CallbackComparisonScore
import com.simprints.id.data.db.event.remote.models.ApiTier
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier

@Keep
class ApiCallbackComparisonScore(val guid: String, val confidence: Int, val tier: ApiTier)

fun CallbackComparisonScore.fromDomainToApi() =
    ApiCallbackComparisonScore(guid, confidence, ApiTier.valueOf(tier.name))

fun ApiCallbackComparisonScore.fromApiToDomain() =
    CallbackComparisonScore(guid, confidence, Tier.valueOf(tier.name))
