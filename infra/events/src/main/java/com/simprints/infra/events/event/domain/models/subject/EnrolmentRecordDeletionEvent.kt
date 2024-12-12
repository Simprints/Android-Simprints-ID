package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class EnrolmentRecordDeletionEvent(
    override val id: String,
    val payload: EnrolmentRecordDeletionPayload,
) : EnrolmentRecordEvent(id, EnrolmentRecordEventType.EnrolmentRecordDeletion) {
    constructor(
        subjectId: String,
        projectId: String,
        moduleId: String,
        attendantId: String,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentRecordDeletionPayload(
            subjectId,
            projectId,
            moduleId,
            attendantId,
        ),
    )

    data class EnrolmentRecordDeletionPayload(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
    )
}
