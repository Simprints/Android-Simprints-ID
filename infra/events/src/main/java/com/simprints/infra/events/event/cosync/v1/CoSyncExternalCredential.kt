package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.serialization.Serializable

/**
 * V1 external schema for external credentials.
 * Stable external contract decoupled from internal [ExternalCredential].
 */
@Keep
@Serializable
data class CoSyncExternalCredential(
    val id: String,
    val value: CoSyncTokenizableString,
    val subjectId: String,
    val type: CoSyncExternalCredentialType,
)

fun ExternalCredential.toCoSync() = CoSyncExternalCredential(
    id = id,
    value = value.toCoSync(),
    subjectId = subjectId,
    type = type.toCoSync(),
)

fun CoSyncExternalCredential.toDomain() = ExternalCredential(
    id = id,
    value = when (val domainValue = value.toDomain()) {
        is TokenizableString.Tokenized -> domainValue
        is TokenizableString.Raw -> error("ExternalCredential value must be Tokenized, got Raw for id=$id")
    },
    subjectId = subjectId,
    type = type.toDomain(),
)
