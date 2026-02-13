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
data class ExternalCredentialV1(
    val id: String,
    val value: TokenizableStringV1,
    val subjectId: String,
    val type: ExternalCredentialTypeV1,
)

fun ExternalCredential.toCoSyncV1() = ExternalCredentialV1(
    id = id,
    value = value.toCoSyncV1(),
    subjectId = subjectId,
    type = type.toCoSyncV1(),
)

fun ExternalCredentialV1.toDomain() = ExternalCredential(
    id = id,
    value = when (val domainValue = value.toDomain()) {
        is TokenizableString.Tokenized -> domainValue
        is TokenizableString.Raw -> error("ExternalCredential value must be Tokenized, got Raw for id=$id")
    },
    subjectId = subjectId,
    type = type.toDomain(),
)
