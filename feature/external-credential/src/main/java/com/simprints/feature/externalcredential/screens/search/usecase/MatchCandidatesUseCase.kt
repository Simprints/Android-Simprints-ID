package com.simprints.feature.externalcredential.screens.search.usecase

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.matching.usecase.FaceMatcherUseCase
import com.simprints.infra.matching.usecase.FingerprintMatcherUseCase
import com.simprints.infra.matching.usecase.MatcherUseCase.MatcherState
import kotlinx.coroutines.flow.last
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
            faceSamples = externalCredentialParams.faceSamples,
            fingerprintSamples = externalCredentialParams.fingerprintSamples,
            ageGroup = externalCredentialParams.ageGroup,
        )
        matchParams
            .mapNotNull { matchParams ->
                when (val sdk = matchParams.bioSdk) {
                    is FaceConfiguration.BioSdk -> {
                        projectConfig.face?.getSdkConfiguration(sdk)?.verificationMatchThreshold?.let { matchThreshold ->
                            (faceMatcher(matchParams, project).last() as? MatcherState.Success)
                                ?.matchResultItems
                                .orEmpty()
                                .map { result ->
                                    CredentialMatch(
                                        credential = credential,
                                        matchResult = result,
                                        verificationThreshold = matchThreshold,
                                        faceBioSdk = sdk,
                                        fingerprintBioSdk = null,
                                    )
                                }
                        }
                    }

                    is FingerprintConfiguration.BioSdk -> {
                        projectConfig.fingerprint?.getSdkConfiguration(sdk)?.verificationMatchThreshold?.let { matchThreshold ->
                            (fingerprintMatcher(matchParams, project).last() as? MatcherState.Success)
                                ?.matchResultItems
                                .orEmpty()
                                .map { result ->
                                    CredentialMatch(
                                        credential = credential,
                                        matchResult = result,
                                        verificationThreshold = matchThreshold,
                                        faceBioSdk = null,
                                        fingerprintBioSdk = sdk,
                                    )
                                }
                        }
                    }

                    else -> null
                }
            }.flatten()
    }
}
