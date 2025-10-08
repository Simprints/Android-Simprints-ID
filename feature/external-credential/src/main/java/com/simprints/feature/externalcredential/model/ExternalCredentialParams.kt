package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.matching.MatchParams

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class ExternalCredentialParams(
    val subjectId: String?,
    val flowType: FlowType,
    val ageGroup: AgeGroup?,
    val probeReferenceId: String?,
    val faceSamples: List<MatchParams.FaceSample>,
    val fingerprintSamples: List<MatchParams.FingerprintSample>,
) : StepParams
