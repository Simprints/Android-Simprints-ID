package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.SCANNER_FIRMWARE_UPDATE
import java.util.UUID

@Keep
data class ScannerFirmwareUpdateEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ScannerFirmwareUpdatePayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        chip: String,
        targetAppVersion: String,
        failureReason: String? = null,
    ) : this(
        UUID.randomUUID().toString(),
        ScannerFirmwareUpdatePayload(
            startTime = startTime,
            eventVersion = EVENT_VERSION,
            endTime = endTime,
            chip = chip,
            targetAppVersion = targetAppVersion,
            failureReason = failureReason,
        ),
        SCANNER_FIRMWARE_UPDATE,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class ScannerFirmwareUpdatePayload(
        override val startTime: Timestamp,
        override val eventVersion: Int,
        override var endTime: Timestamp?,
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
