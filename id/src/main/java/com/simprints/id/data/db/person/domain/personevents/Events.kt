package com.simprints.id.data.db.person.domain.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent
import com.simprints.id.domain.modality.Modes

@Keep
data class Events(val events: List<Event>)

@Keep
data class Event(val id: String,
                 val projectId: List<String>,
                 val subjectId: List<String>,
                 val attendantId: List<String>,
                 val moduleId: List<String>,
                 val mode: List<Modes>,
                 val payload: EventPayload) {
}

fun ApiEvent.fromApiToDomain() = try {
    Event(id,
        labels.getValue(ApiEvent.PROJECT_ID_LABEL),
        labels.getValue(ApiEvent.SUBJECT_ID_LABEL),
        labels.getValue(ApiEvent.ATTENDANT_ID_LABEL),
        labels.getValue(ApiEvent.MODULE_ID_LABEL),
        labels.getValue(ApiEvent.MODE_LABEL).map { Modes.valueOf(it) },
        payload.fromApiToDomain())
} catch (t: Throwable) {
    throw IllegalStateException("Did not get all the labels from cloud")
}
