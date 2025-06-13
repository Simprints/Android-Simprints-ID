package com.simprints.feature.externalcredential.screens.controller

import com.simprints.feature.externalcredential.model.ExternalCredentialResult
import com.simprints.feature.externalcredential.screens.select.OcrLayoutConfig
import javax.inject.Inject
import javax.inject.Singleton

private var ocrConfig = OcrLayoutConfig(
    userMessages = ExternalCredentialResult.entries.associateWith { cardState ->
        when (cardState) {
            ExternalCredentialResult.ENROL_OK -> ""
            ExternalCredentialResult.ENROL_DUPLICATE_FOUND -> "This document belongs to another patient!"
            ExternalCredentialResult.SEARCH_FOUND -> "Patient found"
            ExternalCredentialResult.SEARCH_NOT_FOUND -> "No patient found for this document"
            ExternalCredentialResult.CREDENTIAL_EMPTY -> "Cannot read personal identifier\nRescan or enter manually"
        }
    }.toMutableMap()
)

@Singleton
class OcrLayoutRepository @Inject constructor() {
    var onConfigUpdated: (OcrLayoutConfig) -> Unit = {}
    fun getConfig(): OcrLayoutConfig = ocrConfig
    fun updateUserMessage(message: String, externalCredentialResult: ExternalCredentialResult) {
        ocrConfig = ocrConfig.copy(
            userMessages = ocrConfig.userMessages.toMutableMap().apply {
                this[externalCredentialResult] = message
            }
        )
        onConfigUpdated(ocrConfig)
    }

    fun setConfig(newConfig: OcrLayoutConfig) {
        ocrConfig = newConfig
        onConfigUpdated(ocrConfig)
    }
}
