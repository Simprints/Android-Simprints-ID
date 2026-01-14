package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.step.StepResult
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.matching.MatchResultItem

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class CredentialMatch(
    val credential: TokenizableString.Tokenized,
    val matchResult: MatchResultItem,
    val probeReferenceId: String?,
    val verificationThreshold: Float,
    val faceBioSdk: FaceConfiguration.BioSdk?,
    val fingerprintBioSdk: FingerprintConfiguration.BioSdk?,
) : StepResult {
    val isVerificationSuccessful = matchResult.confidence >= verificationThreshold
    val isFaceMatch = faceBioSdk != null
}
