package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.simprints.core.domain.externalcredential.ExternalCredential

/**
 * V1 external schema for external credentials (e.g., MFID).
 *
 * Represents external identifiers associated with the enrolled subject.
 */
@Keep
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExternalCredentialV1(
    /**
     * Unique identifier for this credential.
     */
    val id: String,
    /**
     * Tokenized credential value (encrypted).
     */
    val value: TokenizableStringV1.Tokenized,
    /**
     * Subject ID this credential belongs to.
     */
    val subjectId: String,
    /**
     * Type of external credential (e.g. GhanaIdCard).
     */
    val type: ExternalCredentialTypeV1,
)

/**
 * Converts internal ExternalCredential to V1 external schema.
 */
fun ExternalCredential.toCoSyncV1() = ExternalCredentialV1(
    id = id,
    value = value.toCoSyncV1() as TokenizableStringV1.Tokenized,
    subjectId = subjectId,
    type = type.toCoSyncV1(),
)

/**
 * Converts V1 external schema to internal ExternalCredential.
 */
fun ExternalCredentialV1.toDomain() = ExternalCredential(
    id = id,
    value = value.toDomain() as com.simprints.core.domain.tokenization.TokenizableString.Tokenized,
    subjectId = subjectId,
    type = type.toDomain(),
)
