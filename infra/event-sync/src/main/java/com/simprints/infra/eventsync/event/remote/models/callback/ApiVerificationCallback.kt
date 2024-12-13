package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep

@Keep
internal data class ApiVerificationCallback(
    val score: ApiCallbackComparisonScore,
) : ApiCallback(ApiCallbackType.Verification)
