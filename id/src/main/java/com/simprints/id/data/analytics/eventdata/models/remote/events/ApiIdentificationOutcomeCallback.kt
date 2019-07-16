package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallback
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallbackType

@Keep
class ApiIdentificationOutcomeCallback : ApiCallback(ApiCallbackType.IDENTIFICATION_OUTCOME)
