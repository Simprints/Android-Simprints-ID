package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.LICENSE_CHECK
import java.util.UUID

@Keep
data class LicenseCheckEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: LicenseCheckEventPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        status: LicenseStatus,
        vendor: String,
    ) : this(
        UUID.randomUUID().toString(),
        LicenseCheckEventPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            status = status,
            vendor = vendor,
        ),
        LICENSE_CHECK,
    )

    enum class LicenseStatus {
        VALID,
        INVALID,
        EXPIRED,
        MISSING,
        ERROR,
    }

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class LicenseCheckEventPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val status: LicenseStatus,
        val vendor: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = LICENSE_CHECK,
    ) : EventPayload() {
        override fun toSafeString(): String = "vendor: $vendor, status: $status"
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
