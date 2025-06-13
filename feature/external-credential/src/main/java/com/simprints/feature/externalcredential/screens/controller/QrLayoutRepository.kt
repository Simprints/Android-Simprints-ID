package com.simprints.feature.externalcredential.screens.controller

import com.simprints.feature.externalcredential.model.ExternalCredentialResult
import com.simprints.feature.externalcredential.screens.select.ExternalCredentialPreviewLayoutConfig
import javax.inject.Inject
import javax.inject.Singleton

private var qrConfig = ExternalCredentialPreviewLayoutConfig(
    userMessages = ExternalCredentialResult.entries.associateWith { cardState ->
        when (cardState) {
            ExternalCredentialResult.ENROL_OK -> "QR Code scanned"
            ExternalCredentialResult.ENROL_DUPLICATE_FOUND -> "This QR belongs to another patient!"
            ExternalCredentialResult.SEARCH_FOUND -> "Patient found"
            ExternalCredentialResult.SEARCH_NOT_FOUND -> "No patient found for this QR"
            ExternalCredentialResult.CREDENTIAL_EMPTY -> "Cannot process QR code data"
        }
    }.toMutableMap()
)

@Singleton
class QrLayoutRepository @Inject constructor() {
    var onConfigUpdated: (ExternalCredentialPreviewLayoutConfig) -> Unit = {}
    fun getConfig(): ExternalCredentialPreviewLayoutConfig = qrConfig
    fun updateUserMessage(message: String, externalCredentialResult: ExternalCredentialResult) {
        qrConfig = qrConfig.copy(
            userMessages = qrConfig.userMessages.toMutableMap().apply {
                this[externalCredentialResult] = message
            }
        )
        onConfigUpdated(qrConfig)
    }

    fun setConfig(newConfig: ExternalCredentialPreviewLayoutConfig) {
        qrConfig = newConfig
        onConfigUpdated(qrConfig)
    }
}
