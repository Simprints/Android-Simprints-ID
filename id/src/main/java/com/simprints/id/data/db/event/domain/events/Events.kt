package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.remote.events.ApiEventNew
import com.simprints.id.domain.modality.Modes

@Keep
data class Events(val events: List<Event>)

@Keep
open class Event(val id: String,
                 val labels: List<EventLabel>,
                 val payload: EventPayload) {

    sealed class EventLabel(val key: String, val labels: List<String>) {

        class ProjectId(labelValue: String) : EventLabel("projectId", listOf(labelValue))
        class SubjectId(labelValue: String) : EventLabel("subjectId", listOf(labelValue))
        class AttendantId(labelValue: String) : EventLabel("attendantId", listOf(labelValue))
        class ModuleId(labelValues: List<String>) : EventLabel("moduleId", labelValues)
        class Mode(labelValues: List<Modes>) : EventLabel("mode", labelValues.map { it.name })
        class SessionId(labelValue: String) : EventLabel("sessionId", listOf(labelValue))
    }

    companion object {
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
