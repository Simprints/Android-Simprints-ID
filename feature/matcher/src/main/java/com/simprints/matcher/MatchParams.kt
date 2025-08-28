package com.simprints.matcher

import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery

@Keep
data class MatchParams(
    val probeReferenceId: String,
    val probeSamples: List<CaptureSample> = emptyList(),
    val sdkType: ModalitySdkType,
    val modality: Modality,
    val flowType: FlowType,
    val queryForCandidates: SubjectQuery,
    val biometricDataSource: BiometricDataSource,
) : StepParams
