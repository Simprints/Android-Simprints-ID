package com.simprints.feature.externalcredential.screens.search.usecase

import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getSdkListForAgeGroup
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
        samples: Map<Modality, List<CaptureSample>>,
        ageGroup: AgeGroup?,
    ): List<MatchParams> = projectConfiguration.general.matchingModalities
        .map { modality ->
            val modalityProbes = samples[modality].orEmpty()
            projectConfiguration.getSdkListForAgeGroup(modality, ageGroup).map {
                MatchParams(
                    probeReferenceId = probeReferenceId.orEmpty(),
                    flowType = flowType,
                    queryForCandidates = SubjectQuery(subjectId = candidateSubjectId),
                    bioSdk = it,
                    probeSamples = modalityProbes,
                    biometricDataSource = BiometricDataSource.Simprints, // [MS-1167] No CoSync in initial MF-ID implementation
                )
            }
        }.flatten()
}
