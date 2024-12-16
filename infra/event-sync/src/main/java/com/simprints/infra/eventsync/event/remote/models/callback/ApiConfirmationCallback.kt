package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep

@Keep
internal data class ApiConfirmationCallback(
    val received: Boolean,
) : ApiCallback(ApiCallbackType.Confirmation)
