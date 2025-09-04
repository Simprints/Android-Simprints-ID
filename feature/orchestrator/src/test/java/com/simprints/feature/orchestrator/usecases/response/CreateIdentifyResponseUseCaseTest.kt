package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.*
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.matching.MatchResult
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
    fun `Returns only face credential results sorted by confidence descending`() = runTest {
        val (faceSmallConfidence, smallConfidence) = "faceSmallConfidence" to 50f
        val (faceBigConfidence, bigConfidence) = "faceBigConfidence" to 99f
        val faceMatches = listOf<CredentialMatch>(
            mockk {
                every { matchResult } returns MatchConfidence(
                    subjectId = faceSmallConfidence,
                    confidence = smallConfidence,
                )
                every { faceBioSdk } returns FaceConfiguration.BioSdk.RANK_ONE
                every { fingerprintBioSdk } returns null
            },
            mockk {
                every { matchResult } returns MatchConfidence(
                    subjectId = faceBigConfidence,
                    confidence = bigConfidence,
                )
                every { faceBioSdk } returns FaceConfiguration.BioSdk.RANK_ONE
                every { fingerprintBioSdk } returns null
            },
        )

        val fingerprintMatches = listOf<CredentialMatch>(
            mockk {
                every { matchResult } returns MatchConfidence(
                    subjectId = "fingerprintSubjectId",
                    confidence = 90f,
                )
                every { faceBioSdk } returns null
                every { fingerprintBioSdk } returns FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
            },
        )

        val result = useCase(
            mockk {
                every { identification.maxNbOfReturnedCandidates } returns 5
                every { face?.getSdkConfiguration(FaceConfiguration.BioSdk.RANK_ONE)?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.getSdkConfiguration(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)?.decisionPolicy } returns
                    DecisionPolicy(20, 50, 100)
            },
            results = listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns faceMatches + fingerprintMatches
                },
            ),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.guid }).isEqualTo(listOf(faceBigConfidence, faceSmallConfidence))
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(bigConfidence.toInt(), smallConfidence.toInt()))
    }

    @Test
    fun `Returns only fingerprint credential results sorted by confidence descending`() = runTest {
        val (fingerprintSmallConfidence, smallConfidence) = "fingerprintSmallConfidence" to 50f
        val (fingerprintBigConfidence, bigConfidence) = "fingerprintBigConfidence" to 99f
        val fingerprintMatches = listOf<CredentialMatch>(
            mockk {
                every { matchResult } returns MatchConfidence(
                    subjectId = fingerprintSmallConfidence,
                    confidence = smallConfidence,
                )
                every { faceBioSdk } returns null
                every { fingerprintBioSdk } returns FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
            },
            mockk {
                every { matchResult } returns MatchConfidence(
                    subjectId = fingerprintBigConfidence,
                    confidence = bigConfidence,
                )
                every { faceBioSdk } returns null
                every { fingerprintBioSdk } returns FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
            },
        )

        val faceMatches = listOf<CredentialMatch>(
            mockk {
                every { matchResult } returns MatchConfidence(
                    subjectId = "faceSubjectId",
                    confidence = 90f,
                )
                every { faceBioSdk } returns FaceConfiguration.BioSdk.RANK_ONE
                every { fingerprintBioSdk } returns null
            },
        )

        val result = useCase(
            mockk {
                every { identification.maxNbOfReturnedCandidates } returns 5
                every { face?.getSdkConfiguration(FaceConfiguration.BioSdk.RANK_ONE)?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.getSdkConfiguration(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)?.decisionPolicy } returns
                    DecisionPolicy(20, 50, 100)
            },
            results = listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns fingerprintMatches + faceMatches
                },
            ),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.guid }).isEqualTo(listOf(fingerprintBigConfidence, fingerprintSmallConfidence))
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(bigConfidence.toInt(), smallConfidence.toInt()))
    }

    @Test
    fun `Returns only credential face results when same ID exists in both credential and face results`() = runTest {
        val sharedGuid = "sharedGuid"
        val credentialConfidence = 95f
        val faceConfidence = 80f

        val credentialFaceMatches = listOf<CredentialMatch>(
            mockk {
                every { matchResult } returns MatchConfidence(
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
                every { face?.getSdkConfiguration(FaceConfiguration.BioSdk.RANK_ONE)?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns null
            },
            results = listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns credentialFaceMatches
                },
                MatchResult(
                    listOf(MatchConfidence(subjectId = sharedGuid, confidence = faceConfidence)),
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
                every { matchResult } returns MatchConfidence(
                    subjectId = sharedGuid,
                    confidence = credentialConfidence,
                )
                every { faceBioSdk } returns null
                every { fingerprintBioSdk } returns FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
            },
        )

        val result = useCase(
            mockk {
                every { identification.maxNbOfReturnedCandidates } returns 5
                every { face?.getSdkConfiguration((any()))?.decisionPolicy } returns null
                every { fingerprint?.getSdkConfiguration(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)?.decisionPolicy } returns
                    DecisionPolicy(20, 50, 100)
            },
            results = listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns credentialFingerprintMatches
                },
                MatchResult(
                    listOf(MatchConfidence(subjectId = sharedGuid, confidence = fingerprintConfidence)),
                    FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                ),
            ),
        )

        assertThat((result as AppIdentifyResponse).identifications).hasSize(1)
        assertThat(result.identifications.first().guid).isEqualTo(sharedGuid)
        assertThat(result.identifications.first().confidenceScore).isEqualTo(credentialConfidence.toInt())
    }

    private fun createFaceMatchResult(vararg confidences: Float): Serializable = MatchResult(
        confidences.mapIndexed { i, confidence -> MatchConfidence(subjectId = "$i", confidence = confidence) },
        FaceConfiguration.BioSdk.RANK_ONE,
    )

    private fun createFingerprintMatchResult(vararg confidences: Float): Serializable = MatchResult(
        confidences.mapIndexed { i, confidence -> MatchConfidence(subjectId = "$i", confidence = confidence) },
        FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
    )
}
