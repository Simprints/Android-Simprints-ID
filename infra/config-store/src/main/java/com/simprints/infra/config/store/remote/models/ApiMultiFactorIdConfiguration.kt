package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.store.models.GhanaIdCardConfig
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.config.store.models.NhisCardConfig
import com.simprints.infra.config.store.models.QrCodeConfig
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.GHANA_CARD
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.NHIS_CARD
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.QR_CODE
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiMultiFactorIdConfiguration(
    val allowedExternalCredentials: List<ApiExternalCredentialType>,
    val ghanaCard: ApiGhanaIdCardConfig?,
    val nhisCard: ApiNhisCardConfig?,
    val qrCode: ApiQrCodeConfig?,
) {
    fun toDomain(): MultiFactorIdConfiguration = MultiFactorIdConfiguration(
        allowedExternalCredentials = allowedExternalCredentials.map { it.toDomain() },
        ghanaIdCardConfig = ghanaCard?.toDomain(),
        nhisCardConfig = nhisCard?.toDomain(),
        qrCodeConfig = qrCode?.toDomain(),
    )
}

@Keep
@Serializable
data class ApiGhanaIdCardConfig(
    val isCapturingAllFields: Boolean,
) {
    fun toDomain() = GhanaIdCardConfig(isCapturingAllFields = isCapturingAllFields)
}

@Keep
@Serializable
data class ApiNhisCardConfig(
    val isCapturingAllFields: Boolean,
) {
    fun toDomain() = NhisCardConfig(isCapturingAllFields = isCapturingAllFields)
}

@Keep
@Serializable
object ApiQrCodeConfig {
    fun toDomain() = QrCodeConfig
}

@Keep
enum class ApiExternalCredentialType {
    NHIS_CARD,
    GHANA_CARD,
    QR_CODE,
    ;

    fun toDomain(): ExternalCredentialType = when (this) {
        NHIS_CARD -> ExternalCredentialType.NHISCard
        GHANA_CARD -> ExternalCredentialType.GhanaIdCard
        QR_CODE -> ExternalCredentialType.QRCode
    }
}

fun ExternalCredentialType.fromDomainToApi(): ApiExternalCredentialType = when (this) {
    ExternalCredentialType.NHISCard -> NHIS_CARD
    ExternalCredentialType.GhanaIdCard -> GHANA_CARD
    ExternalCredentialType.QRCode -> QR_CODE
}
