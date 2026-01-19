package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.step.StepParams
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
@Serializable
@SerialName("ExternalCredentialParams")
data class ExternalCredentialParams(
    val subjectId: String? = null,
    val flowType: FlowType,
    val ageGroup: AgeGroup? = null,
    val probeReferences: List<BiometricReferenceCapture>,
) : StepParams
