package com.simprints.feature.externalcredential.screens.search.usecase

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.usecase.ExternalCredentialEventTrackerUseCase
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
    private val eventsTracker: ExternalCredentialEventTrackerUseCase,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(
        candidates: List<Subject>,
        credential: TokenizableString.Tokenized,
        externalCredentialParams: ExternalCredentialParams,
        project: Project,
        projectConfig: ProjectConfiguration,
    ): List<CredentialMatch> = candidates.flatMap { candidate ->
        val probeReferenceId = externalCredentialParams.probeReferenceId
        val matchParams = createMatchParamsUseCase(
            candidateSubjectId = candidate.subjectId,
            flowType = externalCredentialParams.flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfig,
            faceSamples = externalCredentialParams.faceSamples,
            fingerprintSamples = externalCredentialParams.fingerprintSamples,
            ageGroup = externalCredentialParams.ageGroup,
        )
        matchParams
            .mapNotNull { matchParams ->
                when {
                    matchParams.probeFaceSamples.isNotEmpty() -> {
                        val faceSdk = matchParams.faceSDK ?: return@mapNotNull null
                        projectConfig.face?.getSdkConfiguration(faceSdk)?.verificationMatchThreshold?.let { matchThreshold ->
                            val startTime = timeHelper.now()
                            (faceMatcher(matchParams, project).last() as? MatcherState.Success)
                                ?.matchResultItems
                                .orEmpty()
                                .map { result ->
                                    val match = CredentialMatch(
                                        credential = credential,
                                        matchResult = result,
                                        probeReferenceId = probeReferenceId,
                                        verificationThreshold = matchThreshold,
                                        faceBioSdk = faceSdk,
                                        fingerprintBioSdk = null,
                                    )
                                    eventsTracker.saveMatchEvent(startTime, match)
                                    return@map match
                                }
                        }
                    }

                    else -> {
                        val fingerprintSdk = matchParams.fingerprintSDK ?: return@mapNotNull null
                        projectConfig.fingerprint?.getSdkConfiguration(fingerprintSdk)?.verificationMatchThreshold?.let { matchThreshold ->
                            val startTime = timeHelper.now()
                            (fingerprintMatcher(matchParams, project).last() as? MatcherState.Success)
                                ?.matchResultItems
                                .orEmpty()
                                .map { result ->
                                    val match = CredentialMatch(
                                        credential = credential,
                                        matchResult = result,
                                        probeReferenceId = probeReferenceId,
                                        verificationThreshold = matchThreshold,
                                        faceBioSdk = null,
                                        fingerprintBioSdk = fingerprintSdk,
                                    )
                                    eventsTracker.saveMatchEvent(startTime, match)
                                    return@map match
                                }
                        }
                    }
                }
            }.flatten()
    }
}
