package com.simprints.id.data.db.person.domain.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent

@Keep
class Events(val events: List<Event>)

@Keep
class Event(val id: String,
            val labels: Map<String, List<String>>,
            val payload: EnrolmentRecordOperation) {
    companion object {
        const val PROJECT_ID_LABEL = "projectId"
        const val ATTENDANT_ID_LABEL = "attendantId"
        const val MODULE_ID_LABEL = "moduleId"
    }
}

fun ApiEvent.fromApiToDomain() = Event(id, labels, payload.toDomainEnrolmentRecordOperation())
