package com.simprints.infra.events.event.cosync

import androidx.annotation.Keep
import com.simprints.infra.events.event.cosync.v1.EnrolmentRecordEventV1
import com.simprints.infra.events.event.cosync.v1.toDomain
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
    val events: List<EnrolmentRecordEventV1>,
) : CoSyncEnrolmentRecordEvents {

    override fun toDomainEvents(): List<EnrolmentRecordEvent> = events.map { it.toDomain() }

    companion object {
        const val SCHEMA_VERSION = "1.0"
    }
}
