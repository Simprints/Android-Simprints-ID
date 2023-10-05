package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.SCANNER_FIRMWARE_UPDATE
import java.util.UUID

@Keep
data class ScannerFirmwareUpdateEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ScannerFirmwareUpdatePayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        chip: String,
        targetAppVersion: String,
        failureReason: String? = null,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ScannerFirmwareUpdatePayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            chip = chip,
            targetAppVersion = targetAppVersion,
            failureReason = failureReason
        ),
        SCANNER_FIRMWARE_UPDATE
    )


    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class ScannerFirmwareUpdatePayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val chip: String,
        val targetAppVersion: String,
        var failureReason: String? = null,
        override val type: EventType = SCANNER_FIRMWARE_UPDATE
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
