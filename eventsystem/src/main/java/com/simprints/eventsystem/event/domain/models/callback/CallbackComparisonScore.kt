package com.simprints.eventsystem.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.moduleapi.app.responses.IAppResponseTier

@Keep
data class CallbackComparisonScore(val guid: String, val confidence: Int, val tier: IAppResponseTier)
