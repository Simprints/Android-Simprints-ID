package com.simprints.infra.events.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiVerificationCallback(val score: ApiCallbackComparisonScore): ApiCallback(ApiCallbackType.Verification)
