package com.simprints.feature.externalcredential.screens.search.usecase

import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getSdkListForAgeGroup
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.matching.MatchParams
import javax.inject.Inject

internal class CreateMatchParamsUseCase @Inject constructor() {
    operator fun invoke(
        candidateSubjectId: String,
        flowType: FlowType,
        projectConfiguration: ProjectConfiguration,
        probeReferences: List<BiometricReferenceCapture>,
        ageGroup: AgeGroup?,
    ): List<MatchParams> = probeReferences
        .filter { it.modality in projectConfiguration.general.matchingModalities }
        .associateWith { projectConfiguration.getSdkListForAgeGroup(it.modality, ageGroup) }
        .map { (probeReference, sdksPerModality) ->
            sdksPerModality.map {
                MatchParams(
                    flowType = flowType,
                    queryForCandidates = EnrolmentRecordQuery(subjectId = candidateSubjectId),
                    bioSdk = it,
                    probeReference = probeReference,
                    biometricDataSource = BiometricDataSource.Simprints, // [MS-1167] No CoSync in initial MF-ID implementation
                )
            }
        }.flatten()
}
