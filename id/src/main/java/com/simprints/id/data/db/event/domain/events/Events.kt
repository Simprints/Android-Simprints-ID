package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.domain.modality.Modes

@Keep
data class Events(val events: List<Event>)

@Keep
open class Event(val id: String,
                 val version: Int,
                 val labels: List<EventLabel>,
                 val payload: EventPayload) {

    companion object {
        const val DEFAULT_EVENT_VERSION = 0
    }

    sealed class EventLabel(val key: String) {

        class ProjectId(val labelValue: String) : EventLabel("projectId")
        class SubjectId(val labelValue: String) : EventLabel("subjectId")
        class AttendantId(val labelValue: String) : EventLabel("attendantId")
        class ModuleId(val labelValues: List<String>) : EventLabel("moduleId")
        class Mode(val labelValues: List<Modes>) : EventLabel("mode")
        class SessionId(val labelValue: String) : EventLabel("sessionId")
        class DeviceId(val labelValue: String) : EventLabel("deviceId")
    }
}
