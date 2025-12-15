package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.response.AppMatchConfidence
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class CallbackComparisonScore(
    val guid: String,
    val confidence: Int,
    val confidenceMatch: AppMatchConfidence = AppMatchConfidence.NONE,
)
