package com.simprints.feature.externalcredential

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.step.StepResult
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential

/**
 * Results of the external credential 1:L match (where L is '1' or really close to 1).
 *
 * @param flowType flow type. Either [FlowType.ENROL] or [FlowType.IDENTIFY]
 * @param scannedCredential information about the credential that was scanned
 * @param matchResults if [scannedCredential] exists in local database, this field contains match results between the biometric probe taken
 * during the flow, and probes linked to the [scannedCredential]
 */
@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class ExternalCredentialSearchResult(
    val flowType: FlowType,
    val scannedCredential: ScannedCredential,
    val matchResults: List<CredentialMatch>,
) : StepResult {
    val goodMatches = matchResults.filter(CredentialMatch::isVerificationSuccessful)
}
