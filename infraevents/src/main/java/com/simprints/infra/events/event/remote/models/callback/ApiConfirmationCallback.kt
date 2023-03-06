package com.simprints.infra.events.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiConfirmationCallback(
    val received: Boolean
) : ApiCallback(ApiCallbackType.Confirmation)
