package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
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
                every { face?.decisionPolicy } returns null
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
                every { face?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
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
                every { face?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
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
                every { face?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
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
                every { face?.decisionPolicy } returns null
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
                every { face?.decisionPolicy } returns null
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
                every { face?.decisionPolicy } returns null
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
                every { face?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
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
                every { face?.decisionPolicy } returns DecisionPolicy(20, 50, 100)
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

    private fun createFaceMatchResult(vararg confidences: Float): Serializable = FaceMatchResult(
        confidences.map { FaceMatchResult.Item(subjectId = "1", confidence = it) },
    )

    private fun createFingerprintMatchResult(vararg confidences: Float): Serializable = FingerprintMatchResult(
        confidences.map { FingerprintMatchResult.Item(subjectId = "1", confidence = it) },
        FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
    )
}
