package com.simprints.eventsystem.event.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiConfirmationCallback(
    val received: Boolean
) : ApiCallback(ApiCallbackType.Confirmation)
