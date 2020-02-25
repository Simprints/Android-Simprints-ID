package com.simprints.id.data.db.session.remote.events.callback

import androidx.annotation.Keep

@Keep
class ApiConfirmationCallback(
    val received: Boolean
) : ApiCallback(ApiCallbackType.CONFIRMATION)
