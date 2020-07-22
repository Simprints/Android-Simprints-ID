package com.simprints.id.data.db.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel.*
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.id.domain.modality.Modes
import java.util.*

@Keep
class EnrolmentRecordCreationEvent(
    createdAt: Long,
    subjectId: String,
    projectId: String,
    moduleId: String,
    attendantId: String,
    modes: List<Modes>,
    biometricReferences: List<BiometricReference>
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(ProjectIdLabel(projectId), ModuleIdsLabel(listOf(moduleId)), AttendantIdLabel(attendantId), ModesLabel(modes)),
    EnrolmentRecordCreationPayload(createdAt, EVENT_VERSION, subjectId, projectId, moduleId, attendantId, biometricReferences),
    ENROLMENT_RECORD_CREATION) {

    class EnrolmentRecordCreationPayload(
        createdAt: Long,
        eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<BiometricReference>
    ) : EventPayload(ENROLMENT_RECORD_CREATION, eventVersion, createdAt)
// startTime and relativeStartTime are not used for Pokodex events

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
