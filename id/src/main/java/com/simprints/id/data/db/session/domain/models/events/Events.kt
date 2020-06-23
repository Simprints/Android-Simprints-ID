package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEvent
import com.simprints.id.domain.modality.Modes

@Keep
data class Events(val events: List<Event>)

@Keep
open class Event(val id: String,
                 val labels: List<EventLabel>,
                 val payload: EventPayload)

sealed class EventLabel(val key: String) {
    class ProjectId(labels: String) : EventLabel("projectId")
    class SubjectId(labels: String) : EventLabel("subjectId")
    class AttendantId(labels: String) : EventLabel("attendantId")
    class ModuleId(labels: List<String>) : EventLabel("moduleId")
    class Mode(labels: List<Modes>) : EventLabel("mode")
    class SessionId(labels: String) : EventLabel("sessionId")

    companion object {
        fun fromApiToDomain(labels: Map<String, List<String>>) =
            labels.mapNotNull {
                when (it.key) {
                    ApiEvent.PROJECT_ID_LABEL -> {
                        ProjectId(it.value.first())
                    }
                    ApiEvent.SUBJECT_ID_LABEL -> {
                        SubjectId(it.value.first())
                    }
                    ApiEvent.ATTENDANT_ID_LABEL -> {
                        AttendantId(it.value.first())
                    }
                    ApiEvent.MODULE_ID_LABEL -> {
                        ModuleId(it.value)
                    }
                    ApiEvent.MODE_LABEL -> {
                        Mode(it.value.map { mode -> Modes.valueOf(mode) })
                    }
                    else -> {
                        null
                    }
                }
            }
    }
}

fun ApiEvent.fromApiToDomainOrNullIfNoBiometricReferences() = try {
    Event(id, EventLabel.fromApiToDomain(labels), payload.fromApiToDomainOrNullIfNoBiometricReferences())
} catch (t: Throwable) {
    throw IllegalStateException("Did not get all the labels from cloud")
}
