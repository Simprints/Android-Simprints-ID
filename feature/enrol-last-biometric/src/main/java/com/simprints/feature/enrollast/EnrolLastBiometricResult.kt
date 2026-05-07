package com.simprints.feature.enrollast

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("EnrolLastBiometricResult")
data class EnrolLastBiometricResult(
    val newSubjectId: String?,
    val credentialSearchResult: ExternalCredentialSearchResult.Complete?,
) : StepResult
