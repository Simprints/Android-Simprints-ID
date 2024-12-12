package com.simprints.feature.enrollast.screen.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.MatchResult
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.ProjectConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class HasDuplicateEnrolmentsUseCaseTest {
    private lateinit var useCase: HasDuplicateEnrolmentsUseCase

    @Before
    fun setUp() {
        useCase = HasDuplicateEnrolmentsUseCase()
    }

    @Test
    fun `returns false if duplicate enrolment check is disabled`() {
        val result = useCase(
            projectConfig = mockProjectConfig(false),
            steps = emptyList(),
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false if no fingerprint matches with high score`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = listOf(
                EnrolLastBiometricStepResult.FingerprintMatchResult(
                    listOf(matchResult(LOW_CONFIDENCE)),
                    mockk(),
                ),
            ),
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false if no face matches with high score`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = listOf(
                EnrolLastBiometricStepResult.FaceMatchResult(
                    listOf(
                        matchResult(LOW_CONFIDENCE),
                    ),
                ),
            ),
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false if no fingerprint high score threshold`() {
        val result = useCase(
            projectConfig = mockProjectConfig(highConfidence = null),
            steps = listOf(
                EnrolLastBiometricStepResult.FingerprintMatchResult(
                    listOf(matchResult(HIGH_CONFIDENCE)),
                    mockk(),
                ),
            ),
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false if no face high score threshold`() {
        val result = useCase(
            projectConfig = mockProjectConfig(highConfidence = null),
            steps = listOf(
                EnrolLastBiometricStepResult.FaceMatchResult(
                    listOf(
                        matchResult(HIGH_CONFIDENCE),
                    ),
                ),
            ),
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns true if no previous results provided`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = emptyList(),
        )

        assertThat(result).isTrue()
    }

    @Test
    fun `returns true if there are fingerprint matches with high score`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = listOf(
                EnrolLastBiometricStepResult.FingerprintMatchResult(
                    listOf(matchResult(HIGH_CONFIDENCE)),
                    mockk(),
                ),
            ),
        )

        assertThat(result).isTrue()
    }

    @Test
    fun `returns true if there are face matches with high score`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = listOf(
                EnrolLastBiometricStepResult.FaceMatchResult(
                    listOf(
                        matchResult(HIGH_CONFIDENCE),
                    ),
                ),
            ),
        )

        assertThat(result).isTrue()
    }

    private fun mockProjectConfig(
        checkEnabled: Boolean = true,
        highConfidence: Int? = HIGH_CONFIDENCE_THRESHOLD,
    ): ProjectConfiguration = mockk(relaxed = true) {
        every { general.duplicateBiometricEnrolmentCheck } returns checkEnabled
        // cannot mock Int? directly due to Java inter-op issues, so mocking decision policy instead
        every { fingerprint?.getSdkConfiguration(any())?.decisionPolicy } returns highConfidence?.let {
            DecisionPolicy(0, 0, it)
        }
        every { face?.decisionPolicy } returns highConfidence?.let { DecisionPolicy(0, 0, it) }
    }

    private fun matchResult(confidence: Float) = MatchResult("subjectId", confidence)

    companion object {
        private const val LOW_CONFIDENCE = 50f
        private const val HIGH_CONFIDENCE_THRESHOLD = 100
        private const val HIGH_CONFIDENCE = 150f
    }
}
