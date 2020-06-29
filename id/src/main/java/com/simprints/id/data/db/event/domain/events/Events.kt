package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.remote.events.ApiEventNew
import com.simprints.id.domain.modality.Modes

@Keep
data class Events(val events: List<Event>)

@Keep
open class Event(val id: String,
                 val version: Int,
                 val labels: List<EventLabel>,
                 val payload: EventPayload) {

    sealed class EventLabel(val key: String) {

        class ProjectId(val labelValue: String) : EventLabel("projectId")
        class SubjectId(val labelValue: String) : EventLabel("subjectId")
        class AttendantId(val labelValue: String) : EventLabel("attendantId")
        class ModuleId(val labelValues: List<String>) : EventLabel("moduleId")
        class Mode(val labelValues: List<Modes>) : EventLabel("mode")
        class SessionId(val labelValue: String) : EventLabel("sessionId")
    }

    companion object {
        const val DEFAULT_EVENT_VERSION = 0

        fun fromApiToDomain(labels: Map<String, List<String>>) =
            labels.mapNotNull {
                when (it.key) {
                    ApiEventNew.PROJECT_ID_LABEL -> {
                        ProjectId(it.value.first())
                    }
                    ApiEventNew.SUBJECT_ID_LABEL -> {
                        SubjectId(it.value.first())
                    }
                    ApiEventNew.ATTENDANT_ID_LABEL -> {
                        AttendantId(it.value.first())
                    }
                    ApiEventNew.MODULE_ID_LABEL -> {
                        ModuleId(it.value)
                    }
                    ApiEventNew.MODE_LABEL -> {
                        Mode(it.value.map { mode -> Modes.valueOf(mode) })
                    }
                    else -> {
                        null
                    }
                }
            }
    }
}
