package com.simprints.id.data.analytics.eventdata.models.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier

@Keep
class CallbackComparisonScore(val guid: String, val confidence: Int, val tier: Tier)
