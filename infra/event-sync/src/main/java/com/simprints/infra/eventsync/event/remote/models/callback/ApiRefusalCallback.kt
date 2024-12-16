package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep

@Keep
internal data class ApiRefusalCallback(
    val reason: String,
    val extra: String,
) : ApiCallback(ApiCallbackType.Refusal)
