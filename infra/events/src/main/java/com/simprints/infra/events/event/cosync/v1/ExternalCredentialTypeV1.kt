package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType

/**
 * V1 external schema for external credential types.
 *
 * Represents the type of external credential (e.g., ID cards, QR codes).
 * This is a stable external contract decoupled from internal domain models.
 */
@Keep
enum class ExternalCredentialTypeV1 {
    NHISCard,
    GhanaIdCard,
    QRCode,
}

/**
 * Converts internal ExternalCredentialType to V1 external schema.
 */
fun ExternalCredentialType.toCoSyncV1(): ExternalCredentialTypeV1 = when (this) {
    ExternalCredentialType.NHISCard -> ExternalCredentialTypeV1.NHISCard
    ExternalCredentialType.GhanaIdCard -> ExternalCredentialTypeV1.GhanaIdCard
    ExternalCredentialType.QRCode -> ExternalCredentialTypeV1.QRCode
}

/**
 * Converts V1 external schema to internal ExternalCredentialType.
 */
fun ExternalCredentialTypeV1.toDomain(): ExternalCredentialType = when (this) {
    ExternalCredentialTypeV1.NHISCard -> ExternalCredentialType.NHISCard
    ExternalCredentialTypeV1.GhanaIdCard -> ExternalCredentialType.GhanaIdCard
    ExternalCredentialTypeV1.QRCode -> ExternalCredentialType.QRCode
}
