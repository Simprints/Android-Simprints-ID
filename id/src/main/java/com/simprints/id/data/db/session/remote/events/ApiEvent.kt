package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep

@Keep
abstract class ApiEvent(var type: ApiEventType)
