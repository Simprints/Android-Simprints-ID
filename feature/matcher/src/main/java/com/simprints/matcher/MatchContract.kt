package com.simprints.matcher

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.ModalitySdkType
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.matching.MatchParams

object MatchContract {
    val DESTINATION = R.id.matcherFragment

    fun getParams(
        referenceId: String = "",
        probeSamples: List<CaptureSample> = emptyList(),
        bioSdk: ModalitySdkType,
        flowType: FlowType,
        subjectQuery: SubjectQuery,
        biometricDataSource: BiometricDataSource,
    ) = MatchParams(
        probeReferenceId = referenceId,
        bioSdk = bioSdk,
        probeSamples = probeSamples,
        flowType = flowType,
        queryForCandidates = subjectQuery,
        biometricDataSource = biometricDataSource,
    )
}
