package com.simprints.matcher

import com.simprints.core.domain.common.FlowType
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.matcher.screen.MatchFragmentArgs

object MatchContract {

    val DESTINATION = R.id.matcherFragment

    fun getArgs(
        fingerprintSamples: List<MatchParams.FingerprintSample> = emptyList(),
        faceSamples: List<MatchParams.FaceSample> = emptyList(),
        fingerprintSDK: FingerprintConfiguration.BioSdk? = null,
        flowType: FlowType,
        subjectQuery: SubjectQuery,
        biometricDataSource: BiometricDataSource,
    ) = MatchFragmentArgs(MatchParams(
        faceSamples,
        fingerprintSamples,
        fingerprintSDK,
        flowType,
        subjectQuery,
        biometricDataSource,
    )).toBundle()
}
