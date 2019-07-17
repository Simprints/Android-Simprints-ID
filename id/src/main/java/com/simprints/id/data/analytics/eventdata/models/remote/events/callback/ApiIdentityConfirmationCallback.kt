package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallback
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallbackType

@Keep
class ApiIdentityConfirmationCallback(
    val identificationOutcome: Boolean
) : ApiCallback(ApiCallbackType.IDENTITY_CONFIRMATION)
