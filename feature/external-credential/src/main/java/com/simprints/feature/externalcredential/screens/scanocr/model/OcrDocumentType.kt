package com.simprints.feature.externalcredential.screens.scanocr.model

import com.simprints.core.domain.externalcredential.ExternalCredentialType

enum class OcrDocumentType {
    NhisCard,
    GhanaIdCard,
}

fun ExternalCredentialType.asOcrDocumentType() = when (this) {
    ExternalCredentialType.NHISCard -> OcrDocumentType.NhisCard
    ExternalCredentialType.GhanaIdCard -> OcrDocumentType.GhanaIdCard
    ExternalCredentialType.QRCode -> throw IllegalArgumentException("Cannot create Ocr Document Type from $this")
}

fun OcrDocumentType.asExternalCredentialType() = when (this) {
    OcrDocumentType.NhisCard -> ExternalCredentialType.NHISCard
    OcrDocumentType.GhanaIdCard -> ExternalCredentialType.GhanaIdCard
}
