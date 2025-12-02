package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvents

/**
 * V1 external schema for CoSync enrolment record events.
 *
 * This is a stable external contract that is decoupled from internal domain models.
 * Any changes to this schema MUST maintain backward and forward compatibility, or
 * require a new version (V2, V3, etc.).
 *
 * Compatibility requirements:
 * - Forward compatibility: Old apps must be able to parse data produced by new apps
 * - Backward compatibility: New apps must be able to parse data produced by old apps
 *
 * To ensure forward compatibility:
 * - Never remove or rename existing fields
 * - Only add new optional fields with defaults
 * - Keep field types stable
 */
@Keep
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CoSyncEnrolmentRecordEventsV1(
    /**
     * Schema version for this external contract. Default is "1.0" for V1.
     *
     * This field is optional when deserializing to support old unversioned data.
     * Old apps will ignore this field when deserializing new data.
     */
    @JsonProperty("schemaVersion")
    val schemaVersion: String? = SCHEMA_VERSION,

    /**
     * List of enrolment record events. Currently only supports EnrolmentRecordCreation.
     */
    @JsonProperty("events")
    val events: List<CoSyncEnrolmentRecordEventV1>,
) {
    companion object {
        const val SCHEMA_VERSION = "1.0"
    }
}

/**
 * Converts internal EnrolmentRecordEvents to V1 external schema.
 */
fun EnrolmentRecordEvents.toCoSyncV1() = CoSyncEnrolmentRecordEventsV1(
    schemaVersion = CoSyncEnrolmentRecordEventsV1.SCHEMA_VERSION,
    events = events.map { it.toCoSyncV1() },
)

/**
 * Converts V1 external schema to internal EnrolmentRecordEvents.
 */
fun CoSyncEnrolmentRecordEventsV1.toDomain() = EnrolmentRecordEvents(
    events = events.map { it.toDomain() },
)
