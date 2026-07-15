package com.simprints.feature.externalcredential.screens.search.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.model.CredentialMatch
import kotlin.Boolean

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data struct")
internal data class SearchCredentialState(
    val scannedCredentialResult: ScannedCredentialResult,
    val displayedCredential: TokenizableString.Raw,
    val flowType: FlowType,
    val searchState: SearchState,
    val isConfirmed: Boolean,
    val isEditingCredential: Boolean,
    val isUserConfirmationRequired: Boolean,
    val isEditAvailable: Boolean,
) {
    companion object {
        fun buildInitial(
            scannedCredentialResult: ScannedCredentialResult,
            flowType: FlowType,
        ): SearchCredentialState {
            // [MS-1484] Editing and confirming the readout value should only be available for the OCR readouts (i.e.: Ghana NHIS card)
            // QR codes are not readable by humans, hence there is no point of asking the user to confirm the readout, nor to edit the readout
            val isOcrReadout = when (scannedCredentialResult.credentialType) {
                ExternalCredentialType.NHISCard, ExternalCredentialType.GhanaIdCard, ExternalCredentialType.FaydaCard -> true
                ExternalCredentialType.QRCode -> false
            }
            return SearchCredentialState(
                scannedCredentialResult = scannedCredentialResult,
                displayedCredential = scannedCredentialResult.credential,
                flowType = flowType,
                searchState = SearchState.Searching,
                isConfirmed = false,
                isEditingCredential = false,
                isUserConfirmationRequired = isOcrReadout,
                isEditAvailable = isOcrReadout,
            )
        }
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
