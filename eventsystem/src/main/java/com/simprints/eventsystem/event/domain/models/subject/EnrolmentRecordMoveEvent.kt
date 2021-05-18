package com.simprints.eventsystem.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import java.util.*

@Keep
data class EnrolmentRecordMoveEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentRecordMovePayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        enrolmentRecordCreation: EnrolmentRecordCreationInMove,
        enrolmentRecordDeletion: EnrolmentRecordDeletionInMove,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        EnrolmentRecordMovePayload(createdAt, EVENT_VERSION, enrolmentRecordCreation, enrolmentRecordDeletion),
        EventType.ENROLMENT_RECORD_MOVE
    )

    data class EnrolmentRecordMovePayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val enrolmentRecordCreation: EnrolmentRecordCreationInMove,
        val enrolmentRecordDeletion: EnrolmentRecordDeletionInMove,
        override val type: EventType = EventType.ENROLMENT_RECORD_MOVE,
        override val endedAt: Long = 0
    ) : EventPayload()

    data class EnrolmentRecordDeletionInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String
    )

    data class EnrolmentRecordCreationInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<BiometricReference>?
    )

    companion object {
        const val EVENT_VERSION = 1
    }
}
