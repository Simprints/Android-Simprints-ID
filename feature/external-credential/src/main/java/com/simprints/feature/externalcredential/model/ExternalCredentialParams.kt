package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.step.StepParams

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class ExternalCredentialParams(
    val subjectId: String?,
    val flowType: FlowType,
    val ageGroup: AgeGroup?,
    val probeReferences: List<BiometricReferenceCapture>,
) : StepParams
