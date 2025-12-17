package com.simprints.feature.externalcredential

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.feature.externalcredential.model.ExternalCredentialParams

@ExcludedFromGeneratedTestCoverageReports("Navigation class")
object ExternalCredentialContract {
    val DESTINATION = R.id.externalCredentialControllerFragment

    fun getParams(
        subjectId: String?,
        flowType: FlowType,
        ageGroup: AgeGroup?,
        probeReferences: List<BiometricReferenceCapture> = emptyList(),
    ) = ExternalCredentialParams(
        subjectId = subjectId,
        flowType = flowType,
        ageGroup = ageGroup,
        probeReferences = probeReferences,
    )
}
