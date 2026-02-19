package com.simprints.infra.events.event.cosync

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EnrolmentRecordEvent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Extracts the major version from a schema version string (e.g. "1.2" → "1"). */
private fun String.majorVersion(): String = substringBefore(".")

/**
 * Sealed interface for versioned CoSync enrolment record events.
 *
 * Uses [CoSyncEnrolmentRecordEventsSerializer] to inspect the `schemaVersion`
 * field in JSON and route to the correct version-specific deserializer.
 * Missing `schemaVersion` defaults to V1 for backward compatibility.
 *
 * Versioning convention:
 * - Major version changes (e.g. "1.x" → "2.x") require a new sealed subclass.
 * - Minor version changes (e.g. "1.0" → "1.1") are non-breaking (new optional/nullable
 *   fields) and are handled by the same subclass via `ignoreUnknownKeys` and defaults.
 */
@Keep
@Serializable(with = CoSyncEnrolmentRecordEventsSerializer::class)
sealed interface CoSyncEnrolmentRecordEvents {
    val schemaVersion: String

    /** Converts version-specific models to internal domain events. */
    fun toDomainEvents(): List<EnrolmentRecordEvent>
}

/**
 * Polymorphic serializer that inspects the `schemaVersion` JSON field
 * to select the correct [CoSyncEnrolmentRecordEvents] subclass.
 *
 * Matches on major version only. See [CoSyncEnrolmentRecordEvents] for versioning convention.
 * Defaults to [CoSyncEnrolmentRecordEventsV1] when `schemaVersion` is missing.
 */
internal object CoSyncEnrolmentRecordEventsSerializer :
    JsonContentPolymorphicSerializer<CoSyncEnrolmentRecordEvents>(CoSyncEnrolmentRecordEvents::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CoSyncEnrolmentRecordEvents> {
        val majorVersion = element.jsonObject["schemaVersion"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.majorVersion()

        return when (majorVersion) {
            null, CoSyncEnrolmentRecordEventsV1.SCHEMA_VERSION.majorVersion() -> CoSyncEnrolmentRecordEventsV1.serializer()
            else -> throw IllegalArgumentException(
                "Unknown CoSync schemaVersion: ${element.jsonObject["schemaVersion"]}",
            )
        }
    }
}
