package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import androidx.annotation.Keep

@Keep
class ApiConfirmationCallback(
    val received: Boolean
) : ApiCallback(ApiCallbackType.CONFIRMATION)
