package com.simprints.feature.externalcredential.screens.scanocr.model

import com.simprints.core.domain.externalcredential.ExternalCredentialType

enum class OcrDocumentType {
    NhisCard, GhanaIdCard
}

fun ExternalCredentialType.mapToOcrDocumentType() = when(this) {
    ExternalCredentialType.NHISCard -> OcrDocumentType.NhisCard
    ExternalCredentialType.GhanaIdCard -> OcrDocumentType.GhanaIdCard
    ExternalCredentialType.QRCode -> throw IllegalArgumentException("Cannot create Ocr Document Type from $this")
}

fun OcrDocumentType.mapToCredentialType() = when(this) {
    OcrDocumentType.NhisCard -> ExternalCredentialType.NHISCard
    OcrDocumentType.GhanaIdCard -> ExternalCredentialType.GhanaIdCard
}
