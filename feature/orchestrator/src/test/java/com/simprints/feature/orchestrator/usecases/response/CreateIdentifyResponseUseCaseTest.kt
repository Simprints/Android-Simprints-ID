package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.*
import com.simprints.core.domain.modality.Modality
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.matcher.MatchResult
import com.simprints.matcher.MatchResultItem
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
            mockk(relaxed = true) {
                every { face?.rankOne?.decisionPolicy } returns null
                every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(10f, 20f, 30f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isEmpty()
    }

    @Test
    fun `Returns only face identifications over the low confidence`() = runTest {
        val result = useCase(
            mockk(relaxed = true) {
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.rankOne?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(10f, 20f, 30f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(30, 20))
    }

    @Test
    fun `Returns exactly N best face identifications over the low confidence`() = runTest {
        val result = useCase(
            mockk(relaxed = true) {
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.rankOne?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(20f, 25f, 30f, 40f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(40, 30))
    }

    @Test
    fun `Returns only high confidence face identifications if there are any`() = runTest {
        val result = useCase(
            mockk(relaxed = true) {
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.rankOne?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(15f, 30f, 100f)),
        )

        assertThat((result as AppIdentifyResponse).identifications).isNotEmpty()
        assertThat(result.identifications.map { it.confidenceScore }).isEqualTo(listOf(100))
    }

    @Test
    fun `Returns only fingerprint identifications over the low confidence`() = runTest {
        val result = useCase(
            mockk(relaxed = true) {
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.rankOne?.decisionPolicy } returns null
                every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns DecisionPolicy(
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
            mockk(relaxed = true) {
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.rankOne?.decisionPolicy } returns null
                every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns DecisionPolicy(
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
            mockk(relaxed = true) {
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.rankOne?.decisionPolicy } returns null
                every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns DecisionPolicy(
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
            mockk(relaxed = true) {
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.rankOne?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns DecisionPolicy(
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
            mockk(relaxed = true) {
                every { identification.maxNbOfReturnedCandidates } returns 2
                every { face?.rankOne?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
                every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns DecisionPolicy(
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

    private fun createFaceMatchResult(vararg confidences: Float): Serializable = MatchResult(
        confidences.map { MatchResultItem(subjectId = "1", confidence = it) },
        Modality.FACE,
        FaceConfiguration.BioSdk.RANK_ONE,
    )

    private fun createFingerprintMatchResult(vararg confidences: Float): Serializable = MatchResult(
        confidences.map { MatchResultItem(subjectId = "1", confidence = it) },
        Modality.FINGERPRINT,
        FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
    )
}
