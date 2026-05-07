package com.simprints.feature.enrollast.screen.model

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult

@ExcludedFromGeneratedTestCoverageReports("Data struct")
internal data class CredentialDialogItem(
    val scannedCredentialResult: ScannedCredentialResult,
    val displayedCredential: TokenizableString.Raw,
)
