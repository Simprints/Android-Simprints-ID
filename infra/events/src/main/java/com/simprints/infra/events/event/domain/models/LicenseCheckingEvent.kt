package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.LICENSE_CHECKING
import java.util.UUID

@Keep
data class LicenseCheckingEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: LicenseCheckingEventPayload,
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
        LicenseCheckingEventPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            status = status,
            vendor = vendor
        ),
        LICENSE_CHECKING
    )
    enum class LicenseStatus {
        VALID,
        INVALID,
        MISSING,
        ERROR,
    }

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class LicenseCheckingEventPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val status: LicenseStatus,
        val vendor: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = LICENSE_CHECKING,
    ) : EventPayload()
    companion object {
        const val EVENT_VERSION = 1
    }
}
