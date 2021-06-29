package com.simprints.eventsystem.event.remote.models.callback

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.callback.CallbackComparisonScore
import com.simprints.eventsystem.event.remote.models.ApiTier
import com.simprints.moduleapi.app.responses.IAppResponseTier

@Keep
class ApiCallbackComparisonScore(val guid: String, val confidence: Int, val tier: ApiTier)

fun CallbackComparisonScore.fromDomainToApi() =
    ApiCallbackComparisonScore(guid, confidence, ApiTier.valueOf(tier.name))

fun ApiCallbackComparisonScore.fromApiToDomain() =
    CallbackComparisonScore(guid, confidence, IAppResponseTier.valueOf(tier.name))
