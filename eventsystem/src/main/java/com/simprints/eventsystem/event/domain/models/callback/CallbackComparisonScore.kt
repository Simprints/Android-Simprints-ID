package com.simprints.eventsystem.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier

@Keep
data class CallbackComparisonScore(val guid: String, val confidence: Int, val tier: Tier)
