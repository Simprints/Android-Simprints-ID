package com.simprints.feature.externalcredential.screens.search.usecase

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalitySdkConfig
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
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
        candidates: List<Subject>,
        credential: TokenizableString.Tokenized,
        externalCredentialParams: ExternalCredentialParams,
        project: Project,
        projectConfig: ProjectConfiguration,
    ): List<CredentialMatch> = candidates.flatMap { candidate ->
        val matchParams = createMatchParamsUseCase(
            candidateSubjectId = candidate.subjectId,
            flowType = externalCredentialParams.flowType,
            probeReferenceId = externalCredentialParams.probeReferenceId,
            projectConfiguration = projectConfig,
            samples = externalCredentialParams.samples,
            ageGroup = externalCredentialParams.ageGroup,
        )
        matchParams
            .mapNotNull { matchParam ->
                val matchThreshold = projectConfig
                    .getModalitySdkConfig(matchParam.bioSdk)
                    ?.verificationMatchThreshold
                    ?: return@mapNotNull null
                val lastMatchSuccess = when (matchParam.bioSdk) {
                    is FaceConfiguration.BioSdk -> faceMatcher(matchParam, project).lastOrNull() as? MatcherState.Success
                    is FingerprintConfiguration.BioSdk -> fingerprintMatcher(matchParam, project).lastOrNull() as? MatcherState.Success
                    else -> null
                }
                lastMatchSuccess?.comparisonResults?.map { result ->
                    CredentialMatch(
                        credential = credential,
                        matchResult = result,
                        verificationThreshold = matchThreshold,
                        bioSdk = matchParam.bioSdk,
                    )
                }
            }.flatten()
    }
}
