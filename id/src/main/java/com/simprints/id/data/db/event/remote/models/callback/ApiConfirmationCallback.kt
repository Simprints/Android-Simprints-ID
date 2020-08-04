package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep

@Keep
data class ApiConfirmationCallback(
    val received: Boolean
) : ApiCallback(ApiCallbackType.Confirmation)
