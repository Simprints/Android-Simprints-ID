package com.simprints.matcher

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery

object MatchContract {
    val DESTINATION = R.id.matcherFragment

    fun getParams(
        referenceId: String = "",
        probeSamples: List<CaptureSample> = emptyList(),
        modality: Modality,
        sdkType: ModalitySdkType,
        flowType: FlowType,
        subjectQuery: SubjectQuery,
        biometricDataSource: BiometricDataSource,
    ) = MatchParams(
        probeReferenceId = referenceId,
        probeSamples = probeSamples,
        modality = modality,
        sdkType = sdkType,
        flowType = flowType,
        queryForCandidates = subjectQuery,
        biometricDataSource = biometricDataSource,
    )
}
