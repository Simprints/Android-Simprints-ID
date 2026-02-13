package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import kotlinx.serialization.Serializable

/**
 * V1 external schema for external credential types.
 * Stable external contract decoupled from internal [ExternalCredentialType].
 */
@Keep
@Serializable
enum class ExternalCredentialTypeV1 {
    NHISCard,
    GhanaIdCard,
    QRCode,
}

fun ExternalCredentialType.toCoSyncV1(): ExternalCredentialTypeV1 = when (this) {
    ExternalCredentialType.NHISCard -> ExternalCredentialTypeV1.NHISCard
    ExternalCredentialType.GhanaIdCard -> ExternalCredentialTypeV1.GhanaIdCard
    ExternalCredentialType.QRCode -> ExternalCredentialTypeV1.QRCode
}

fun ExternalCredentialTypeV1.toDomain(): ExternalCredentialType = when (this) {
    ExternalCredentialTypeV1.NHISCard -> ExternalCredentialType.NHISCard
    ExternalCredentialTypeV1.GhanaIdCard -> ExternalCredentialType.GhanaIdCard
    ExternalCredentialTypeV1.QRCode -> ExternalCredentialType.QRCode
}
