package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.ModalitySdkType
import com.simprints.core.domain.sample.MatchComparisonResult
import com.simprints.core.domain.step.StepResult
import com.simprints.core.domain.tokenization.TokenizableString

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class CredentialMatch(
    val credential: TokenizableString.Tokenized,
    val matchResult: MatchComparisonResult,
    val verificationThreshold: Float,
    val bioSdk: ModalitySdkType,
) : StepResult {
    val isVerificationSuccessful = matchResult.confidence >= verificationThreshold
}
