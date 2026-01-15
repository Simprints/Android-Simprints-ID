package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName("EnrolmentRecordDeletion")
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class EnrolmentRecordDeletionEvent(
    override val id: String,
    val payload: EnrolmentRecordDeletionPayload,
) : EnrolmentRecordEvent() {
    override val type: EnrolmentRecordEventType
        get() = EnrolmentRecordEventType.EnrolmentRecordDeletion
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

    @Keep
    @Serializable
    data class EnrolmentRecordDeletionPayload(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
    )
}
