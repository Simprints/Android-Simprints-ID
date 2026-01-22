package com.simprints.feature.externalcredential.screens.search.usecase

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalitySdkConfig
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.matching.usecase.FaceMatcherUseCase
import com.simprints.infra.matching.usecase.FingerprintMatcherUseCase
import com.simprints.infra.matching.usecase.MatcherUseCase.MatcherState
import kotlinx.coroutines.flow.lastOrNull
import javax.inject.Inject

internal class MatchCandidatesUseCase @Inject constructor(
    private val createMatchParamsUseCase: CreateMatchParamsUseCase,
    private val faceMatcher: FaceMatcherUseCase,
    private val fingerprintMatcher: FingerprintMatcherUseCase,
) {
    suspend operator fun invoke(
        candidates: List<EnrolmentRecord>,
        credential: TokenizableString.Tokenized,
        externalCredentialParams: ExternalCredentialParams,
        project: Project,
        projectConfig: ProjectConfiguration,
    ): List<CredentialMatch> = candidates.flatMap { candidate ->
        val matchParams = createMatchParamsUseCase(
            candidateSubjectId = candidate.subjectId,
            flowType = externalCredentialParams.flowType,
            projectConfiguration = projectConfig,
            probeReferences = externalCredentialParams.probeReferences,
            ageGroup = externalCredentialParams.ageGroup,
        )
        matchParams
            .mapNotNull { matchParam ->
                val matchThreshold = projectConfig
                    .getModalitySdkConfig(matchParam.bioSdk)
                    ?.verificationMatchThreshold
                    ?: return@mapNotNull null
                val lastMatchSuccess = when (matchParam.bioSdk.modality()) {
                    Modality.FACE -> faceMatcher(matchParam, project).lastOrNull() as? MatcherState.Success
                    Modality.FINGERPRINT -> fingerprintMatcher(matchParam, project).lastOrNull() as? MatcherState.Success
                }
                lastMatchSuccess?.comparisonResults?.map { result ->
                    CredentialMatch(
                        credential = credential,
                        comparisonResult = result,
                        verificationThreshold = matchThreshold,
                        bioSdk = matchParam.bioSdk,
                    )
                }
            }.flatten()
    }
}
