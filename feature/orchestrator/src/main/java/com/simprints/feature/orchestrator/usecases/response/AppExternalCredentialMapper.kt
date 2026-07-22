package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.screens.search.model.MfidDocument
import com.simprints.infra.orchestration.data.responses.AppExternalCredential

internal fun ExternalCredentialSearchResult.Complete?.toAppExternalCredential(): AppExternalCredential? {
    if (this == null) return null
    val nonCredentialFields: Map<String, String> = when (val document = scannedCredentialResult.document) {
        is MfidDocument.GhanaIdCard -> document.toNonCredentialFields()
        is MfidDocument.GhanaNhisCard -> document.toNonCredentialFields()
        is MfidDocument.GhanaQrCode -> emptyMap()
        is MfidDocument.FaydaCard -> emptyMap()
    }
    return AppExternalCredential(
        id = scannedCredentialResult.credentialScanId,
        value = confirmedCredential,
        type = scannedCredentialResult.credentialType,
        nonCredentialFields = nonCredentialFields,
    )
}

private fun MfidDocument.GhanaNhisCard.toNonCredentialFields(): Map<String, String> = buildMap {
    putIfPresent(GhanaNhisCardFields.NAME, name)
    putIfPresent(GhanaNhisCardFields.DATE_OF_BIRTH, dateOfBirth)
    putIfPresent(GhanaNhisCardFields.SEX, sex)
    putIfPresent(GhanaNhisCardFields.DATE_OF_ISSUE, dateOfIssue)
}

private fun MfidDocument.GhanaIdCard.toNonCredentialFields(): Map<String, String> = buildMap {
    putIfPresent(GhanaIdCardFields.SURNAME, surname)
    putIfPresent(GhanaIdCardFields.FIRST_NAME, firstName)
    putIfPresent(GhanaIdCardFields.NATIONALITY, nationality)
    putIfPresent(GhanaIdCardFields.DATE_OF_BIRTH, dateOfBirth)
    putIfPresent(GhanaIdCardFields.HEIGHT, height)
    putIfPresent(GhanaIdCardFields.DOCUMENT_NUMBER, documentNumber)
    putIfPresent(GhanaIdCardFields.PLACE_OF_ISSUE, placeOfIssue)
    putIfPresent(GhanaIdCardFields.DATE_OF_ISSUE, dateOfIssue)
    putIfPresent(GhanaIdCardFields.DATE_OF_EXPIRY, dateOfExpiry)
}

private fun MutableMap<String, String>.putIfPresent(
    key: String,
    value: TokenizableString.Raw?,
) {
    value?.let { put(key, it.value) }
}

private object GhanaNhisCardFields {
    const val NAME = "name"
    const val DATE_OF_BIRTH = "dateOfBirth"
    const val SEX = "sex"
    const val DATE_OF_ISSUE = "dateOfIssue"
}

private object GhanaIdCardFields {
    const val SURNAME = "surname"
    const val FIRST_NAME = "firstName"
    const val NATIONALITY = "nationality"
    const val DATE_OF_BIRTH = "dateOfBirth"
    const val HEIGHT = "height"
    const val DOCUMENT_NUMBER = "documentNumber"
    const val PLACE_OF_ISSUE = "placeOfIssue"
    const val DATE_OF_ISSUE = "dateOfIssue"
    const val DATE_OF_EXPIRY = "dateOfExpiry"
}
