package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.NOT_AUTHORIZED
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.infra.eventsync.event.remote.models.ApiAuthorizationPayload.ApiResult
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiAuthorizationPayload(
    override val startTime: ApiTimestamp,
    val result: ApiResult,
    val userInfo: ApiUserInfo?,
) : ApiEventPayload() {
    @Keep
    @Serializable
    data class ApiUserInfo(
        val projectId: String,
        val userId: String,
    ) {
        constructor(userInfoDomain: UserInfo) :
            this(userInfoDomain.projectId, userInfoDomain.userId.value)
    }

    @Keep
    @Serializable
    enum class ApiResult {
        AUTHORIZED,
        NOT_AUTHORIZED,
    }

    constructor(domainPayload: AuthorizationPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.result.fromDomainToApi(),
        domainPayload.userInfo?.let { ApiUserInfo(it) },
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = when (tokenKeyType) {
        TokenKeyType.AttendantId -> "userInfo.userId"
        else -> null
    }
}

internal fun AuthorizationPayload.AuthorizationResult.fromDomainToApi() = when (this) {
    AUTHORIZED -> ApiResult.AUTHORIZED
    NOT_AUTHORIZED -> ApiResult.NOT_AUTHORIZED
}
