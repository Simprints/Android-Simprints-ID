package com.simprints.infra.matching

import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.ModalitySdkType
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery

@Keep
data class MatchParams(
    val probeReferenceId: String,
    val bioSdk: ModalitySdkType,
    val probeFaceSamples: List<CaptureSample> = emptyList(),
    val probeFingerprintSamples: List<CaptureSample> = emptyList(),
    val flowType: FlowType,
    val queryForCandidates: SubjectQuery,
    val biometricDataSource: BiometricDataSource,
) : StepParams {
    fun isFaceMatch() = probeFaceSamples.isNotEmpty()
}
