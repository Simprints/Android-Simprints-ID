package com.simprints.infra.events.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.core.domain.response.AppMatchConfidence

@Keep
data class CallbackComparisonScore(
    val guid: String,
    val confidence: Int,
    val confidenceMatch: AppMatchConfidence = AppMatchConfidence.NONE,
)
