package com.simprints.id.data.db.person.domain.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent

@Keep
class Events(val events: List<Event>)

@Keep
class Event(val id: String,
            val labels: Map<String, List<String>>,
            val payload: EnrolmentRecordOperation)

fun ApiEvent.fromApiToDomain() = Event(id, labels, payload.toDomainEnrolmentRecordOperation())
