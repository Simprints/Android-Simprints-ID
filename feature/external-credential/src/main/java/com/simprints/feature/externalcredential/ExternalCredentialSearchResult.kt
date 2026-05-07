package com.simprints.feature.externalcredential

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.step.StepResult
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("ExternalCredentialSearchResult")
@ExcludedFromGeneratedTestCoverageReports("Data class")
sealed class ExternalCredentialSearchResult : StepResult {
    abstract val flowType: FlowType

    @Serializable
    @SerialName("ExternalCredentialSearchResult.Skipped")
    data class Skipped(
        override val flowType: FlowType,
        val skipReason: ExternalCredentialSelectionEvent.SkipReason,
    ) : ExternalCredentialSearchResult()

    /**
     * Results of the external credential 1:L match (where L is '1' or really close to 1).
     *
     * @param flowType flow type. Either [FlowType.ENROL] or [FlowType.IDENTIFY]
     * @param scannedCredentialResult information about the credential that was scanned
     * @param confirmedCredential credential value that was confirmed by the user. Might be different from the credential value in the
     * [scannedCredentialResult]
     * @param matchResults if [scannedCredentialResult] exists in local database, this field contains match results between the biometric probe taken
     * during the flow, and probes linked to the [scannedCredentialResult]
     */
    @Serializable
    @SerialName("ExternalCredentialSearchResult.Complete")
    data class Complete(
        override val flowType: FlowType,
        val scannedCredentialResult: ScannedCredentialResult,
        val confirmedCredential: TokenizableString.Raw,
        val matchResults: List<CredentialMatch>,
    ) : ExternalCredentialSearchResult() {
        val goodMatches = matchResults.filter(CredentialMatch::isVerificationSuccessful)
    }
}
