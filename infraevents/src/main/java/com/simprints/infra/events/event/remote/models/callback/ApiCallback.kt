package com.simprints.infra.events.remote.models.callback

import androidx.annotation.Keep

@Keep
abstract class ApiCallback(var type: ApiCallbackType)
