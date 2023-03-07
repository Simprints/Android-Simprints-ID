package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep

@Keep
abstract class ApiCallback(var type: ApiCallbackType)
