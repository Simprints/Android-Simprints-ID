package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep

@Keep
class ApiEvents(val events: Array<ApiEvent>)

@Keep
class ApiEvent(val id: String, val labels: Array<String>, val payload: ApiEnrolmentRecordOperation)
