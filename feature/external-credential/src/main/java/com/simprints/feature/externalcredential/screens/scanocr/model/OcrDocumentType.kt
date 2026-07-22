package com.simprints.feature.externalcredential.screens.scanocr.model

import com.simprints.core.domain.externalcredential.ExternalCredentialType

enum class OcrDocumentType {
    NhisCard,
    GhanaIdCard,
    FaydaCard,
}

fun ExternalCredentialType.asOcrDocumentType() = when (this) {
    ExternalCredentialType.NHISCard -> OcrDocumentType.NhisCard
    ExternalCredentialType.GhanaIdCard -> OcrDocumentType.GhanaIdCard
    ExternalCredentialType.FaydaCard -> OcrDocumentType.FaydaCard
    ExternalCredentialType.QRCode -> throw IllegalArgumentException("Cannot create Ocr Document Type from $this")
}

fun OcrDocumentType.asExternalCredentialType() = when (this) {
    OcrDocumentType.NhisCard -> ExternalCredentialType.NHISCard
    OcrDocumentType.GhanaIdCard -> ExternalCredentialType.GhanaIdCard
    OcrDocumentType.FaydaCard -> ExternalCredentialType.FaydaCard
}
