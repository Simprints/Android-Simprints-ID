package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.AgeGroup

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class ExternalCredentialParams(
    val subjectId: String?,
    val flowType: FlowType,
    val ageGroup: AgeGroup?,
    val probeReferenceId: String?,
    val samples: Map<Modality, List<CaptureSample>>,
) : StepParams
