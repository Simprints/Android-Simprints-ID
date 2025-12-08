package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.matching.FaceMatchResult
import com.simprints.infra.matching.FingerprintMatchResult
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.Serializable

class CreateIdentifyResponseUseCaseTest {
    @MockK
    lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: CreateIdentifyResponseUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.getCurrentSessionScope().id } returns "sessionId"

        useCase = CreateIdentifyResponseUseCase(eventRepository)
    }

    @Test
    fun `Returns no identifications if no decision policy`() = runTest {
        val result = useCase(
            mockk {
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { multifactorId?.allowedExternalCredentials } returns null
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns null
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(10f, 20f, 30f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isEmpty()
    }

    @Test
    fun `Returns only face identifications over the low confidence`() = runTest {
        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(10f, 20f, 30f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(30, 20))
    }

    @Test
    fun `Returns exactly N best face identifications over the low confidence`() = runTest {
        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(20f, 25f, 30f, 40f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(40, 30))
    }

    @Test
    fun `Returns only high confidence face identifications if there are any`() = runTest {
        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(15f, 30f, 100f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(100))
    }

    @Test
    fun `Returns only fingerprint identifications over the low confidence`() = runTest {
        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns null
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    20,
                    50,
                    100,
                )
            },
            results = listOf(createFingerprintMatchResult(10f, 20f, 30f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(30, 20))
    }

    @Test
    fun `Returns exactly N best fingerprint identifications over the low confidence`() = runTest {
        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns null
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    20,
                    50,
                    100,
                )
            },
            results = listOf(createFingerprintMatchResult(20f, 25f, 30f, 40f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(40, 30))
    }

    @Test
    fun `Returns only high confidence fingerprint identifications if there are any`() = runTest {
        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns null
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    20,
                    50,
                    100,
                )
            },
            results = listOf(createFingerprintMatchResult(15f, 30f, 100f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(100))
    }

    @Test
    fun `Returns fingerprint matches if both modalities available and fingerprint has higher confidence`() = runTest {
        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    20,
                    50,
                    100,
                )
            },
            results = listOf(
                createFaceMatchResult(15f, 30f, 100f),
                createFingerprintMatchResult(15f, 30f, 105f),
            ),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(105))
    }

    @Test
    fun `Returns face matches if both modalities available and face has higher confidence`() = runTest {
        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    20,
                    50,
                    100,
                )
            },
            results = listOf(
                createFaceMatchResult(15f, 30f, 105f),
                createFingerprintMatchResult(15f, 30f, 100f),
            ),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(105))
    }

    @Test
    fun `Returns both fingerprint and face credential results sorted by confidence descending`() = runTest {
        val (fingerprintSmallConfidenceGUID, smallConfidence) = "fingerprintSmallConfidenceGUID" to 50f
        val (fingerprintBigConfidenceGUID, fingerprintBigConfidence) = "fingerprintBigConfidenceGUID" to 99f
        val (faceBigConfidenceGUID, faceBigConfidence) = "faceBigConfidenceGUID" to fingerprintBigConfidence - 1
        val fingerprintMatches = listOf<CredentialMatch>(
            mockk {
                every { verificationThreshold } returns 0.0f
                every { matchResult } returns FingerprintMatchResult.Item(
                    subjectId = fingerprintSmallConfidenceGUID,
                    confidence = smallConfidence,
                )
                every { faceBioSdk } returns null
                every { fingerprintBioSdk } returns FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
            },
            mockk {
                every { matchResult } returns FingerprintMatchResult.Item(
                    subjectId = fingerprintBigConfidenceGUID,
                    confidence = fingerprintBigConfidence,
                )
                every { faceBioSdk } returns null
                every { fingerprintBioSdk } returns FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
            },
        )

        val faceMatches = listOf<CredentialMatch>(
            mockk {
                every { matchResult } returns FaceMatchResult.Item(
                    subjectId = faceBigConfidenceGUID,
                    confidence = faceBigConfidence,
                )
                every { faceBioSdk } returns FaceConfiguration.BioSdk.RANK_ONE
                every { fingerprintBioSdk } returns null
            },
        )

        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 5
                every { face?.getSdkConfiguration(FaceConfiguration.BioSdk.RANK_ONE)?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { face?.getSdkConfiguration(FaceConfiguration.BioSdk.RANK_ONE)?.verificationMatchThreshold } returns 0.0f
                every { fingerprint?.getSdkConfiguration(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)?.decisionPolicy } returns
                    DecisionPolicy(20, 50, 100)
                every {
                    fingerprint
                        ?.getSdkConfiguration(
                            FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                        )?.verificationMatchThreshold
                } returns
                    0.0f
            },
            results = listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns fingerprintMatches + faceMatches
                },
            ),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(
            result.identifications.map {
                it.guid
            },
        ).isEqualTo(listOf(faceBigConfidenceGUID, fingerprintBigConfidenceGUID, fingerprintSmallConfidenceGUID))
        assertThat(
            result.identifications.map {
                it.confidenceScore
            },
        ).isEqualTo(listOf(faceBigConfidence.toInt(), fingerprintBigConfidence.toInt(), smallConfidence.toInt()))
    }

    @Test
    fun `Returns only credential face results when same ID exists in both credential and face results`() = runTest {
        val sharedGuid = "sharedGuid"
        val credentialConfidence = 95f
        val faceConfidence = 80f

        val credentialFaceMatches = listOf<CredentialMatch>(
            mockk {
                every { matchResult } returns FaceMatchResult.Item(
                    subjectId = sharedGuid,
                    confidence = credentialConfidence,
                )
                every { faceBioSdk } returns FaceConfiguration.BioSdk.RANK_ONE
                every { fingerprintBioSdk } returns null
            },
        )

        val result = useCase(
            mockk {
                every { identification.maxNbOfReturnedCandidates } returns 5
                every { multifactorId?.allowedExternalCredentials } returns null
                every { face?.getSdkConfiguration(FaceConfiguration.BioSdk.RANK_ONE)?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { face?.getSdkConfiguration(FaceConfiguration.BioSdk.RANK_ONE)?.verificationMatchThreshold } returns 0.0f
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns null
            },
            results = listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns credentialFaceMatches
                },
                FaceMatchResult(
                    listOf(
                        FaceMatchResult.Item(subjectId = sharedGuid, confidence = faceConfidence),
                    ),
                    FaceConfiguration.BioSdk.RANK_ONE,
                ),
            ),
        )

        assertThat((result as AppIdentifyResponse).identifications).hasSize(1)
        assertThat(result.identifications.first().guid).isEqualTo(sharedGuid)
        assertThat(result.identifications.first().confidenceScore).isEqualTo(credentialConfidence.toInt())
    }

    @Test
    fun `Returns only credential fingerprint results when same ID exists in both credential and fingerprint results`() = runTest {
        val sharedGuid = "sharedGuid"
        val credentialConfidence = 95f
        val fingerprintConfidence = 80f

        val credentialFingerprintMatches = listOf<CredentialMatch>(
            mockk {
                every { verificationThreshold } returns 0.0f
                every { matchResult } returns FingerprintMatchResult.Item(
                    subjectId = sharedGuid,
                    confidence = credentialConfidence,
                )
                every { faceBioSdk } returns null
                every { fingerprintBioSdk } returns FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
            },
        )

        val result = useCase(
            mockk {
                every { multifactorId?.allowedExternalCredentials } returns null
                every { identification.maxNbOfReturnedCandidates } returns 5
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns null
                every { fingerprint?.getSdkConfiguration(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)?.decisionPolicy } returns
                    DecisionPolicy(20, 50, 100)
                every {
                    fingerprint
                        ?.getSdkConfiguration(
                            FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                        )?.verificationMatchThreshold
                } returns
                    0.0f
            },
            results = listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns credentialFingerprintMatches
                },
                FingerprintMatchResult(
                    listOf(
                        FingerprintMatchResult.Item(subjectId = sharedGuid, confidence = fingerprintConfidence),
                    ),
                    FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                ),
            ),
        )

        assertThat((result as AppIdentifyResponse).identifications).hasSize(1)
        assertThat(result.identifications.first().guid).isEqualTo(sharedGuid)
        assertThat(result.identifications.first().confidenceScore).isEqualTo(credentialConfidence.toInt())
    }

    private fun createFaceMatchResult(vararg confidences: Float): Serializable = FaceMatchResult(
        confidences.mapIndexed { i, confidence -> FaceMatchResult.Item(subjectId = "$i", confidence = confidence) },
        FaceConfiguration.BioSdk.RANK_ONE,
    )

    private fun createFingerprintMatchResult(vararg confidences: Float): Serializable = FingerprintMatchResult(
        confidences.mapIndexed { i, confidence -> FingerprintMatchResult.Item(subjectId = "$i", confidence = confidence) },
        FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
    )
}
