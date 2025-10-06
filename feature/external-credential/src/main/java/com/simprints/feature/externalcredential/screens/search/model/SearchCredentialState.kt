package com.simprints.feature.externalcredential.screens.search.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.model.CredentialMatch

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data struct")
internal data class SearchCredentialState(
    val scannedCredential: ScannedCredential,
    val displayedCredential: TokenizableString.Raw?,
    val flowType: FlowType,
    val searchState: SearchState,
    val isConfirmed: Boolean,
) {
    companion object {
        fun buildInitial(scannedCredential: ScannedCredential, flowType: FlowType) =
            SearchCredentialState(
                scannedCredential = scannedCredential,
                displayedCredential = null,
                flowType = flowType,
                searchState = SearchState.Searching,
                isConfirmed = false
            )
    }
}

@Keep
@ExcludedFromGeneratedTestCoverageReports("State data class")
internal sealed class SearchState {
    data object Searching : SearchState()

    data class CredentialLinked(
        val matchResults: List<CredentialMatch>,
    ) : SearchState() {
        val goodMatches = matchResults.filter(CredentialMatch::isVerificationSuccessful)
        val hasSuccessfulVerifications: Boolean = goodMatches.isNotEmpty()
    }

    data object CredentialNotFound : SearchState()
}
