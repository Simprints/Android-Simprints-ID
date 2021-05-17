package com.simprints.eventsystem.event.remote.models.callback

import androidx.annotation.Keep

@Keep
abstract class ApiCallback(var type: ApiCallbackType)
