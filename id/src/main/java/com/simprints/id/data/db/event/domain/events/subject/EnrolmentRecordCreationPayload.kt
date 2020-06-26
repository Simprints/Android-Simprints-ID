package com.simprints.id.data.db.event.domain.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class EnrolmentRecordCreationEvent(
    subjectId: String,
    projectId: String,
    moduleId: String,
    attendantId: String,
    biometricReferences: List<BiometricReference>
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.ProjectId(projectId), EventLabel.ModuleId(listOf(moduleId)), EventLabel.AttendantId(attendantId)),
    EnrolmentRecordCreationPayload(subjectId, projectId, moduleId, attendantId, biometricReferences)) {

    data class EnrolmentRecordCreationPayload(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<BiometricReference>
    ) : EventPayload(EventPayloadType.ENROLMENT_RECORD_CREATION, 0)
// startTime and relativeStartTime are not used for Pokodex events
}
