package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.matching.MatchResultItem

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class CredentialMatch(
    val credential: String,
    val matchResult: MatchResultItem,
    val verificationThreshold: Float,
    val faceBioSdk: FaceConfiguration.BioSdk?,
    val fingerprintBioSdk: FingerprintConfiguration.BioSdk?
) {
    val isVerificationSuccessful = matchResult.confidence >= verificationThreshold
}
