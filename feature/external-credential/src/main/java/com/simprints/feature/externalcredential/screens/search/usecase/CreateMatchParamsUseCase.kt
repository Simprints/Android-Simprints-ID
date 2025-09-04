package com.simprints.feature.externalcredential.screens.search.usecase

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.determineFaceSDKs
import com.simprints.infra.config.store.models.determineFingerprintSDKs
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.matching.MatchParams
import javax.inject.Inject

internal class CreateMatchParamsUseCase @Inject constructor() {
    operator fun invoke(
        candidateSubjectId: String,
        flowType: FlowType,
        probeReferenceId: String?,
        projectConfiguration: ProjectConfiguration,
        faceSamples: List<CaptureSample>,
        fingerprintSamples: List<CaptureSample>,
        ageGroup: AgeGroup?,
    ): List<MatchParams> = projectConfiguration.general.matchingModalities
        .map { modality ->
            val template = MatchParams(
                probeReferenceId = probeReferenceId.orEmpty(),
                flowType = flowType,
                queryForCandidates = SubjectQuery(subjectId = candidateSubjectId),
                biometricDataSource = BiometricDataSource.Simprints, // [MS-1167] No CoSync in initial MF-ID implementation
            )
            when (modality) {
                Modality.FACE ->
                    projectConfiguration
                        .determineFaceSDKs(ageGroup)
                        .map { template.copy(faceSDK = it, probeFaceSamples = faceSamples) }

                Modality.FINGERPRINT ->
                    projectConfiguration
                        .determineFingerprintSDKs(ageGroup)
                        .map { template.copy(fingerprintSDK = it, probeFingerprintSamples = fingerprintSamples) }
            }
        }.flatten()
}
