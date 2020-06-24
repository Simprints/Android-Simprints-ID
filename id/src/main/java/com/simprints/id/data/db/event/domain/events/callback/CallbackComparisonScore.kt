package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier

@Keep
class CallbackComparisonScore(val guid: String, val confidence: Int, val tier: Tier)
