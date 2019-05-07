package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiVerificationCallback

@Keep
class VerificationCallback(val score: CallbackComparisonScore): Callback(CallbackType.VERIFICATION)

fun VerificationCallback.toApiVerificationCallback() =
    ApiVerificationCallback(score.toApiCallbackComparisonScore())
