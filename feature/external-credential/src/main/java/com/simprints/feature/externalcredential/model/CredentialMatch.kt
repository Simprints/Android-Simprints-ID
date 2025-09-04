package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.core.domain.step.StepResult
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class CredentialMatch(
    val credential: TokenizableString.Tokenized,
    val matchResult: MatchConfidence,
    val verificationThreshold: Float,
    val faceBioSdk: FaceConfiguration.BioSdk?,
    val fingerprintBioSdk: FingerprintConfiguration.BioSdk?,
) : StepResult {
    val isVerificationSuccessful = matchResult.confidence >= verificationThreshold
}
