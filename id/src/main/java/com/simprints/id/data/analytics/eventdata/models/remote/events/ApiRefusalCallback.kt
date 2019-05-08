package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep

@Keep
class ApiRefusalCallback(val reason: String, val extra: String): ApiCallback(ApiCallbackType.REFUSAL)
