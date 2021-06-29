package com.simprints.eventsystem.event.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiVerificationCallback(val score: ApiCallbackComparisonScore): ApiCallback(ApiCallbackType.Verification)
