package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.Event

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
class ApiEvent(val id: String,
               val labels: List<String>,
               val payload: ApiEnrolmentRecordOperation)

fun Event.fromDomainToApi() =
    ApiEvent(id, labels, payload.fromDomainToApi())

