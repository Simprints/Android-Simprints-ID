package com.simprints.id.data.db.person.domain.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent

@Keep
data class Events(val events: List<Event>)

@Keep
data class Event(val id: String,
            val labels: Map<String, List<String>>,
            val payload: EnrolmentRecordOperation) {
    companion object {
        const val PROJECT_ID_LABEL = "projectId"
        const val ATTENDANT_ID_LABEL = "attendantId"
        const val MODULE_ID_LABEL = "moduleId"
        const val SUBJECT_ID_LABEL = "subjectId"
        const val MODE_LABEL = "mode"
    }
}

fun ApiEvent.fromApiToDomain() = Event(id, labels, payload.toDomainEnrolmentRecordOperation())
