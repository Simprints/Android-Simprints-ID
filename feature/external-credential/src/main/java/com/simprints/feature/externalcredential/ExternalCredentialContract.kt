package com.simprints.feature.externalcredential

import com.simprints.core.domain.common.FlowType
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.matching.MatchParams

object ExternalCredentialContract {
    val DESTINATION = R.id.externalCredentialControllerFragment

    fun getParams(
        subjectId: String?,
        flowType: FlowType,
        ageGroup: AgeGroup?,
        probeReferenceId: String? = null,
        faceSamples: List<MatchParams.FaceSample> = emptyList(),
        fingerprintSamples: List<MatchParams.FingerprintSample> = emptyList(),
    ) = ExternalCredentialParams(
        subjectId = subjectId,
        flowType = flowType,
        ageGroup = ageGroup,
        probeReferenceId = probeReferenceId,
        faceSamples = faceSamples,
        fingerprintSamples = fingerprintSamples
    )

}
