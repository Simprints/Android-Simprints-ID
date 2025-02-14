package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.infra.events.event.domain.models.EventType.AUTHORIZATION
import java.util.UUID

@Keep
data class AuthorizationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: AuthorizationPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        result: AuthorizationResult,
        userInfo: UserInfo?,
    ) : this(
        UUID.randomUUID().toString(),
        AuthorizationPayload(createdAt, EVENT_VERSION, result, userInfo),
        AUTHORIZATION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = if (payload.userInfo == null) {
        emptyMap()
    } else {
        mapOf(TokenKeyType.AttendantId to payload.userInfo.userId)
    }

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this.copy(
        payload = payload.copy(
            userInfo = payload.userInfo?.copy(
                userId = map[TokenKeyType.AttendantId] ?: payload.userInfo.userId,
            ),
        ),
    )

    @Keep
    data class AuthorizationPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val result: AuthorizationResult,
        val userInfo: UserInfo?,
        override val endedAt: Timestamp? = null,
        override val type: EventType = AUTHORIZATION,
    ) : EventPayload() {
        override fun toSafeString(): String = "result: $result"

        @Keep
        enum class AuthorizationResult {
            AUTHORIZED,
            NOT_AUTHORIZED,
        }

        @Keep
        data class UserInfo(
            val projectId: String,
            val userId: TokenizableString,
        )
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
