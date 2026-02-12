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
enum class CoSyncExternalCredentialType {
    NHISCard,
    GhanaIdCard,
    QRCode,
}

fun ExternalCredentialType.toCoSync(): CoSyncExternalCredentialType = when (this) {
    ExternalCredentialType.NHISCard -> CoSyncExternalCredentialType.NHISCard
    ExternalCredentialType.GhanaIdCard -> CoSyncExternalCredentialType.GhanaIdCard
    ExternalCredentialType.QRCode -> CoSyncExternalCredentialType.QRCode
}

fun CoSyncExternalCredentialType.toDomain(): ExternalCredentialType = when (this) {
    CoSyncExternalCredentialType.NHISCard -> ExternalCredentialType.NHISCard
    CoSyncExternalCredentialType.GhanaIdCard -> ExternalCredentialType.GhanaIdCard
    CoSyncExternalCredentialType.QRCode -> ExternalCredentialType.QRCode
}
