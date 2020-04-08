package com.simprints.id.data.db.person.domain.personevents

import androidx.annotation.Keep

@Keep
class Events(val events: List<Event>)

@Keep
class Event(val id: String,
            val labels: List<String>,
            val payload: EnrolmentRecordOperation)
