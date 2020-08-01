package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep

@Keep
class ApiRefusalCallback(val reason: String, val extra: String): ApiCallback(ApiCallbackType.Refusal)
