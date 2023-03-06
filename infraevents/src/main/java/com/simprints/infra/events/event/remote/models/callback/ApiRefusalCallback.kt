package com.simprints.infra.events.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiRefusalCallback(val reason: String, val extra: String): ApiCallback(ApiCallbackType.Refusal)
