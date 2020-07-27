package com.simprints.id.data.db.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.id.domain.modality.Modes
import java.util.*

@Keep
data class EnrolmentRecordCreationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentRecordCreationPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        subjectId: String,
        projectId: String,
        moduleId: String,
        attendantId: String,
        modes: List<Modes>,
        biometricReferences: List<BiometricReference>,
        labels: EventLabels = EventLabels() //StopShip
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        EnrolmentRecordCreationPayload(createdAt, EVENT_VERSION, subjectId, projectId, moduleId, attendantId, biometricReferences),
        ENROLMENT_RECORD_CREATION)

    data class EnrolmentRecordCreationPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<BiometricReference>
    ) : EventPayload(ENROLMENT_RECORD_CREATION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
