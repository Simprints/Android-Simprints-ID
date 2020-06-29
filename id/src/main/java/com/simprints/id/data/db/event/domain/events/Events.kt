package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.remote.events.ApiEventNew
import com.simprints.id.domain.modality.Modes

@Keep
data class Events(val events: List<Event>)

@Keep
open class Event(val id: String,
                 val labels: List<EventLabel>,
                 val payload: EventPayload)

sealed class EventLabel(val key: String, val labels: List<String>) {

    class ProjectId(label: String) : EventLabel("projectId", listOf(label))
    class SubjectId(label: String) : EventLabel("subjectId", listOf(label))
    class AttendantId(label: String) : EventLabel("attendantId", listOf(label))
    class ModuleId(labels: List<String>) : EventLabel("moduleId", labels)
    class Mode(labels: List<Modes>) : EventLabel("mode", labels.map { it.name })
    class SessionId(label: String) : EventLabel("sessionId", listOf(label))
}
