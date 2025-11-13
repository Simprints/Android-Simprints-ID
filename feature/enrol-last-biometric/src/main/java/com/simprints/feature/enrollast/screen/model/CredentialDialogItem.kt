package com.simprints.feature.enrollast.screen.model

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential

@ExcludedFromGeneratedTestCoverageReports("Data struct")
internal data class CredentialDialogItem(
    val scannedCredential: ScannedCredential,
    val displayedCredential: TokenizableString.Raw,
)
