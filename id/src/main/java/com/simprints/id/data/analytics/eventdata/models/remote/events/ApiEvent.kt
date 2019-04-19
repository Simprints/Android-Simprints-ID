package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep

@Keep
abstract class ApiEvent(var type: ApiEventType)
