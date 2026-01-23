package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.domain.step.StepResult
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.ModalitySdkType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
@Serializable
@SerialName("CredentialMatch")
data class CredentialMatch(
    val credential: TokenizableString.Tokenized,
    val comparisonResult: ComparisonResult,
    val probeReferenceId: String?,
    val matcherName: String,
    val verificationThreshold: Float,
    val bioSdk: ModalitySdkType,
) : StepResult {
    val isVerificationSuccessful = comparisonResult.comparisonScore >= verificationThreshold
}
