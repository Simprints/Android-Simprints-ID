package com.simprints.feature.externalcredential.screens.search.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.FlowType
import com.simprints.feature.externalcredential.model.CredentialMatch

@Keep
internal data class SearchCredentialState(
    val scannedCredential: ScannedCredential,
    val flowType: FlowType,
    val searchState: SearchState,
    val isConfirmed: Boolean,
) {
    companion object {
        fun buildInitial(scannedCredential: ScannedCredential, flowType: FlowType) =
            SearchCredentialState(
                scannedCredential = scannedCredential,
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
