package com.simprints.id.data.db.subject.domain.subjectevents

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEvent
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

fun ApiEvent.fromApiToDomainOrNullIfNoBiometricReferences() = try {
    payload.fromApiToDomainOrNullIfNoBiometricReferences()?.let {
        Event(id,
            labels.getValue(ApiEvent.PROJECT_ID_LABEL),
            labels.getValue(ApiEvent.SUBJECT_ID_LABEL),
            labels.getValue(ApiEvent.ATTENDANT_ID_LABEL),
            labels.getValue(ApiEvent.MODULE_ID_LABEL),
            labels.getValue(ApiEvent.MODE_LABEL).map { Modes.valueOf(it) },
            it)
    }
} catch (t: Throwable) {
    throw IllegalStateException("Did not get all the labels from cloud")
}
