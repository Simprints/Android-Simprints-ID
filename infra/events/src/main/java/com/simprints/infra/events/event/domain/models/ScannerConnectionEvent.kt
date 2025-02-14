package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.SCANNER_CONNECTION
import java.util.UUID

@Keep
data class ScannerConnectionEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ScannerConnectionPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        scannerInfo: ScannerConnectionPayload.ScannerInfo,
    ) : this(
        UUID.randomUUID().toString(),
        ScannerConnectionPayload(createdAt, EVENT_VERSION, scannerInfo),
        SCANNER_CONNECTION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class ScannerConnectionPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val scannerInfo: ScannerInfo,
        override val endedAt: Timestamp? = null,
        override val type: EventType = SCANNER_CONNECTION,
    ) : EventPayload() {
        override fun toSafeString(): String = "scanner: ${scannerInfo.scannerId}, mac: ${scannerInfo.macAddress}, " +
            "generation: ${scannerInfo.generation}, hardware version: ${scannerInfo.hardwareVersion}"

        @Keep
        data class ScannerInfo(
            val scannerId: String,
            val macAddress: String,
            val generation: ScannerGeneration,
            var hardwareVersion: String?,
        )

        @Keep
        enum class ScannerGeneration {
            VERO_1,
            VERO_2,
        }
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
