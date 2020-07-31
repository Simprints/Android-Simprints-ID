package com.simprints.id.data.db.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_MOVE
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import com.simprints.id.data.db.event.remote.models.subject.ApiBiometricReference
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
        enrolmentRecordCreation: EnrolmentRecordCreationInMove?,
        enrolmentRecordDeletion: EnrolmentRecordDeletionInMove,
        labels: EventLabels = EventLabels() //StopShip
    ) : this(
        UUID.randomUUID().toString(),
        labels, //STOPSHIP: to check with cloud - labels empty?
        EnrolmentRecordMovePayload(createdAt, DEFAULT_EVENT_VERSION, enrolmentRecordCreation, enrolmentRecordDeletion),
        ENROLMENT_RECORD_MOVE)

    data class EnrolmentRecordMovePayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val enrolmentRecordCreation: EnrolmentRecordCreationInMove?,
        val enrolmentRecordDeletion: EnrolmentRecordDeletionInMove,
        override val type: EventType = ENROLMENT_RECORD_MOVE,
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
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
