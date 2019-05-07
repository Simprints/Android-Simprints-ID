package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiRefusalCallback

@Keep
class RefusalCallback(val reason: String, val extra: String): Callback(CallbackType.REFUSAL)

fun RefusalCallback.toApiRefusalCallback() = ApiRefusalCallback(reason, extra)
