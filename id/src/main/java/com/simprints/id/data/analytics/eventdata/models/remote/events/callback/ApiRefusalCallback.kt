package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import io.realm.internal.Keep

@Keep
class ApiRefusalCallback(val reason: String,
                         val extra: String) : ApiCallback(ApiCallbackType.REFUSAL)

