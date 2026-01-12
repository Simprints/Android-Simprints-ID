package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiConfirmationCallback(
    val received: Boolean,
    override val type: ApiCallbackType = ApiCallbackType.Confirmation,
) : ApiCallback()
