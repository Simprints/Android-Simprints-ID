package com.simprints.matcher

import com.simprints.core.domain.common.FlowType
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.matching.MatchParams

object MatchContract {
    val DESTINATION = R.id.matcherFragment

    fun getParams(
        referenceId: String = "",
        fingerprintSamples: List<MatchParams.FingerprintSample> = emptyList(),
        faceSamples: List<MatchParams.FaceSample> = emptyList(),
        fingerprintSDK: FingerprintConfiguration.BioSdk? = null,
        faceSDK: FaceConfiguration.BioSdk? = null,
        flowType: FlowType,
        subjectQuery: SubjectQuery,
        biometricDataSource: BiometricDataSource,
    ) = MatchParams(
        referenceId,
        faceSamples,
        faceSDK,
        fingerprintSamples,
        fingerprintSDK,
        flowType,
        subjectQuery,
        biometricDataSource,
    )
}
