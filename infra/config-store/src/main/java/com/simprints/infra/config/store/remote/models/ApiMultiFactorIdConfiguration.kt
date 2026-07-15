package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.store.models.FaydaCardConfig
import com.simprints.infra.config.store.models.GhanaIdCardConfig
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.config.store.models.NhisCardConfig
import com.simprints.infra.config.store.models.QrCodeConfig
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.FAYDA_CARD
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.GHANA_CARD
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.NHIS_CARD
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.QR_CODE
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiMultiFactorIdConfiguration(
    val allowedExternalCredentials: List<ApiExternalCredentialType>,
    val ghanaCard: ApiGhanaIdCardConfig? = null,
    val nhisCard: ApiNhisCardConfig? = null,
    val qrCode: ApiQrCodeConfig? = null,
    val faydaCard: ApiFaydaCardConfig? = null,
) {
    fun toDomain(): MultiFactorIdConfiguration = MultiFactorIdConfiguration(
        allowedExternalCredentials = allowedExternalCredentials.map { it.toDomain() },
        ghanaIdCardConfig = ghanaCard?.toDomain(),
        nhisCardConfig = nhisCard?.toDomain(),
        qrCodeConfig = qrCode?.toDomain(),
        faydaCardConfig = faydaCard?.toDomain(),
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
@Serializable
data class ApiFaydaCardConfig(
    val isCapturingAllFields: Boolean,
) {
    fun toDomain() = FaydaCardConfig(isCapturingAllFields = isCapturingAllFields)
}

@Keep
enum class ApiExternalCredentialType {
    NHIS_CARD,
    GHANA_CARD,
    QR_CODE,
    FAYDA_CARD,
    ;

    fun toDomain(): ExternalCredentialType = when (this) {
        NHIS_CARD -> ExternalCredentialType.NHISCard
        GHANA_CARD -> ExternalCredentialType.GhanaIdCard
        QR_CODE -> ExternalCredentialType.QRCode
        FAYDA_CARD -> ExternalCredentialType.FaydaCard
    }
}

fun ExternalCredentialType.fromDomainToApi(): ApiExternalCredentialType = when (this) {
    ExternalCredentialType.NHISCard -> NHIS_CARD
    ExternalCredentialType.GhanaIdCard -> GHANA_CARD
    ExternalCredentialType.QRCode -> QR_CODE
    ExternalCredentialType.FaydaCard -> FAYDA_CARD
}
