package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppVerifyResponse
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.io.Serializable

class CreateVerifyResponseUseCaseTest {

    private lateinit var useCase: CreateVerifyResponseUseCase

    @Before
    fun setUp() {
        useCase = CreateVerifyResponseUseCase()
    }

    @Test
    fun `Returns error if no decision policy`() {
        val result = useCase(
            mockk {
                every { face } returns null
                every { fingerprint?.getSdkConfiguration((any())) } returns null
            },
            results = listOf(createFaceMatchResult(10f, 20f, 30f))
        )

        assertThat(result).isInstanceOf(AppErrorResponse::class.java)
    }

    @Test
    fun `Returns face matches with highest score`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns null
                every { face?.verificationMatchThreshold } returns null
                every { fingerprint?.getSdkConfiguration((any()))?.verificationMatchThreshold } returns null
            },
            results = listOf(createFaceMatchResult(10f, 50f, 100f))
        )

        assertThat((result as AppVerifyResponse).matchResult.confidenceScore).isEqualTo(100)
    }

    @Test
    fun `Returns fingerprint matches with highest score`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns null
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    10,
                    20,
                    30
                )
                every { fingerprint?.getSdkConfiguration((any()))?.verificationMatchThreshold } returns 50f
            },
            results = listOf(createFingerprintMatchResult(10f, 50f, 100f))
        )

        assertThat((result as AppVerifyResponse).matchResult.confidenceScore).isEqualTo(100)
    }

    @Test
    fun `Returns matches with highest face match score`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    10,
                    20,
                    30
                )
                every { face?.verificationMatchThreshold } returns 50f
                every { fingerprint?.getSdkConfiguration((any()))?.verificationMatchThreshold } returns 50f
            },
            results = listOf(
                createFaceMatchResult(10f, 50f, 105f),
                createFingerprintMatchResult(10f, 50f, 100f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.confidenceScore).isEqualTo(105)
    }

    @Test
    fun `Returns matches with highest fingerprint match score`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    10,
                    20,
                    30
                )
                every { face?.verificationMatchThreshold } returns 50f
                every { fingerprint?.getSdkConfiguration((any()))?.verificationMatchThreshold } returns 50f
            },
            results = listOf(
                createFaceMatchResult(10f, 50f, 100f),
                createFingerprintMatchResult(10f, 50f, 105f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.confidenceScore).isEqualTo(105)
    }

    @Test
    fun `When face verificationMatchThreshold is null - verificationSuccess is null`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { face?.verificationMatchThreshold } returns null
            },
            results = listOf(
                createFaceMatchResult(10f, 50f, 100f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.verificationSuccess).isNull()
    }

    @Test
    fun `When fingerprint verificationMatchThreshold is null - verificationSuccess is null`() {
        val result = useCase(
            mockk {
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    10,
                    20,
                    30
                )
                every { fingerprint?.getSdkConfiguration((any()))?.verificationMatchThreshold } returns null
            },
            results = listOf(
                createFingerprintMatchResult(10f, 50f, 100f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.verificationSuccess).isNull()
    }

    @Test
    fun `When face match score is above verificationMatchThreshold - verificationSuccess is true`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { face?.verificationMatchThreshold } returns 50f
            },
            results = listOf(
                createFaceMatchResult(51f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.verificationSuccess).isEqualTo(true)
    }

    @Test
    fun `When fingerprint match score is above verificationMatchThreshold - verificationSuccess is true`() {
        val result = useCase(
            mockk {
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    10,
                    20,
                    30
                )
                every { fingerprint?.getSdkConfiguration((any()))?.verificationMatchThreshold } returns 50f
            },
            results = listOf(
                createFingerprintMatchResult(51f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.verificationSuccess).isEqualTo(true)
    }

    @Test
    fun `When face match score is equal to verificationMatchThreshold - verificationSuccess is true`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { face?.verificationMatchThreshold } returns 50f
            },
            results = listOf(
                createFaceMatchResult(50f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.verificationSuccess).isEqualTo(true)
    }

    @Test
    fun `When fingerprint match score is equal to verificationMatchThreshold - verificationSuccess is true`() {
        val result = useCase(
            mockk {
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    10,
                    20,
                    30
                )
                every { fingerprint?.getSdkConfiguration((any()))?.verificationMatchThreshold } returns 50f
            },
            results = listOf(
                createFingerprintMatchResult(50f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.verificationSuccess).isEqualTo(true)
    }

    @Test
    fun `When face match score is below verificationMatchThreshold - verificationSuccess is false`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { face?.verificationMatchThreshold } returns 50f
            },
            results = listOf(
                createFaceMatchResult(49f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.verificationSuccess).isEqualTo(false)
    }

    @Test
    fun `When fingerprint match score is below verificationMatchThreshold - verificationSuccess is false`() {
        val result = useCase(
            mockk {
                every { fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns DecisionPolicy(
                    10,
                    20,
                    30
                )
                every { fingerprint?.getSdkConfiguration((any()))?.verificationMatchThreshold } returns 50f
            },
            results = listOf(
                createFingerprintMatchResult(49f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.verificationSuccess).isEqualTo(false)
    }

    private fun createFingerprintMatchResult(vararg confidences: Float): Serializable = FingerprintMatchResult(
        confidences.map { FingerprintMatchResult.Item(subjectId = "1", confidence = it) },
        mockk(),
    )

    private fun createFaceMatchResult(vararg confidences: Float): Serializable = FaceMatchResult(
        confidences.map { FaceMatchResult.Item(subjectId = "1", confidence = it) }
    )
}
