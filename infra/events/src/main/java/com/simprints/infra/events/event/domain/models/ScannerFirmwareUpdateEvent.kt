package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.EventType.Companion.SCANNER_FIRMWARE_UPDATE_KEY
import com.simprints.infra.events.event.domain.models.EventType.SCANNER_FIRMWARE_UPDATE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(SCANNER_FIRMWARE_UPDATE_KEY)
data class ScannerFirmwareUpdateEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ScannerFirmwareUpdatePayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endTime: Timestamp,
        chip: String,
        targetAppVersion: String,
        failureReason: String? = null,
    ) : this(
        UUID.randomUUID().toString(),
        ScannerFirmwareUpdatePayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            chip = chip,
            targetAppVersion = targetAppVersion,
            failureReason = failureReason,
        ),
        SCANNER_FIRMWARE_UPDATE,
    )

    @Keep
    @Serializable
    data class ScannerFirmwareUpdatePayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        override var endedAt: Timestamp?,
        val chip: String,
        val targetAppVersion: String,
        var failureReason: String? = null,
        override val type: EventType = SCANNER_FIRMWARE_UPDATE,
    ) : EventPayload() {
        override fun toSafeString(): String = "chip: $chip, target version: $targetAppVersion, failure reason: $failureReason"
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
