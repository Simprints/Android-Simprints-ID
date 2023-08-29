package com.simprints.infra.events.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppResponseTier

@Keep
data class CallbackComparisonScore(
    val guid: String,
    val confidence: Int,
    val tier: IAppResponseTier,
    val confidenceMatch: IAppMatchConfidence?,
)
