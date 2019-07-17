package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallback
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallbackType

@Keep
class ApiRefusalCallback(val reason: String, val extra: String): ApiCallback(ApiCallbackType.REFUSAL)
