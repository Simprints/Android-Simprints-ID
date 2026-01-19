package com.simprints.feature.enrollast

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("EnrolLastBiometricResult")
data class EnrolLastBiometricResult(
    val newSubjectId: String?,
    val externalCredential: ExternalCredential?,
) : StepResult
