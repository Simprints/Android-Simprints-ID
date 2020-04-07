package com.simprints.id.data.db.person.domain.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordOperation

@Keep
class Events(val events: Array<Event>)

@Keep
class Event(val id: String,
               vararg labels: String,
               val payload: EnrolmentRecordOperation)
