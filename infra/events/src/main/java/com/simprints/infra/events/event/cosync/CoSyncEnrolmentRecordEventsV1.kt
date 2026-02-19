package com.simprints.infra.events.event.cosync

import androidx.annotation.Keep
import com.simprints.infra.events.event.cosync.v1.CoSyncEnrolmentRecordEvent
import com.simprints.infra.events.event.cosync.v1.toEventDomain
import com.simprints.infra.events.event.domain.models.EnrolmentRecordEvent
import kotlinx.serialization.Serializable

/**
 * V1 schema for CoSync enrolment record events.
 *
 * Compatibility:
 * - Old JSON without `schemaVersion` is deserialized as V1 (backward compatible).
 * - Serialized JSON includes `schemaVersion = "1.0"` (forward compatible).
 */
@Keep
@Serializable
data class CoSyncEnrolmentRecordEventsV1(
    override val schemaVersion: String = SCHEMA_VERSION,
    val events: List<CoSyncEnrolmentRecordEvent>,
) : CoSyncEnrolmentRecordEvents {

    override fun toDomainEvents(): List<EnrolmentRecordEvent> = events.map { it.toEventDomain() }

    companion object {
        const val SCHEMA_VERSION = "1.0"
    }
}
