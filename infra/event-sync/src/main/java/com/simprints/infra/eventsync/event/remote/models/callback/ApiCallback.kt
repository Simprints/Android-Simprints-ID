package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep

@Keep
internal abstract class ApiCallback(
    var type: ApiCallbackType,
)
