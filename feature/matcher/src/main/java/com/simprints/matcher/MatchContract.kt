package com.simprints.matcher

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery

object MatchContract {
    val DESTINATION = R.id.matcherFragment

    fun getParams(
        referenceId: String = "",
        probeSamples: List<CaptureSample> = emptyList(),
        modality: Modality,
        fingerprintSDK: FingerprintConfiguration.BioSdk? = null,
        faceSDK: FaceConfiguration.BioSdk? = null,
        flowType: FlowType,
        subjectQuery: SubjectQuery,
        biometricDataSource: BiometricDataSource,
    ) = MatchParams(
        probeReferenceId = referenceId,
        probeSamples = probeSamples,
        modality = modality,
        faceSDK = faceSDK,
        fingerprintSDK = fingerprintSDK,
        flowType = flowType,
        queryForCandidates = subjectQuery,
        biometricDataSource = biometricDataSource,
    )
}
