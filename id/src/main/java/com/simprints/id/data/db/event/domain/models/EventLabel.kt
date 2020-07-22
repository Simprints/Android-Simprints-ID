package com.simprints.id.data.db.event.domain.models

import com.simprints.id.data.db.event.domain.models.EventLabel.EventLabelKey.*
import com.simprints.id.domain.modality.Modes

abstract class EventLabel(val key: EventLabelKey, val values: List<String>) {

    enum class EventLabelKey(val key: String) {
        PROJECT_ID("projectId"),
        SUBJECT_ID("subjectId"),
        ATTENDANT_ID("attendantId"),
        MODULE_IDS("moduleIds"),
        MODES("modes"),
        SESSION_ID("sessionId"),
        DEVICE_ID("deviceId")
    }

    data class ProjectIdLabel(val projectId: String) : EventLabel(PROJECT_ID, listOf(projectId))
    data class SubjectIdLabel(val subjectId: String) : EventLabel(SUBJECT_ID, listOf(subjectId))
    data class AttendantIdLabel(val attendantId: String) : EventLabel(ATTENDANT_ID, listOf(attendantId))
    data class ModuleIdsLabel(val moduleIds: List<String>) : EventLabel(MODULE_IDS, moduleIds)
    data class ModesLabel(val mode: List<Modes>) : EventLabel(MODES, mode.map { it.name })
    data class SessionIdLabel(val sessionId: String) : EventLabel(SESSION_ID, listOf(sessionId))
    data class DeviceIdLabel(val deviceId: String) : EventLabel(DEVICE_ID, listOf(deviceId))
}
