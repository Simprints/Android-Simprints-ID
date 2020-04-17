package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.Event
import com.simprints.id.data.db.person.domain.personevents.Events

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
class ApiEvent(val id: String,
               val labels: Map<String, List<String>>,
               val payload: ApiEnrolmentRecordOperation)

fun Events.fromDomainToApi() = ApiEvents(events.map { it.fromDomainToApi() })

fun Event.fromDomainToApi() =
    ApiEvent(id, labels, payload.fromDomainToApi())

