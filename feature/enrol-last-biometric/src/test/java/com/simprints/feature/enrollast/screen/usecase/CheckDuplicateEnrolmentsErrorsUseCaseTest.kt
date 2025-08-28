package com.simprints.feature.enrollast.screen.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.modality.Modality
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.screen.EnrolLastState
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import io.mockk.*
import org.junit.Before
import org.junit.Test

class CheckDuplicateEnrolmentsErrorsUseCaseTest {
    private lateinit var useCase: CheckForDuplicateEnrolmentsUseCase

    @Before
    fun setUp() {
        useCase = CheckForDuplicateEnrolmentsUseCase()
    }

    @Test
    fun `returns false if duplicate enrolment check is disabled`() {
        val result = useCase(
            projectConfig = mockProjectConfig(false),
            steps = emptyList(),
        )

        assertThat(result).isNull()
    }

    @Test
    fun `returns false if no fingerprint matches with high score`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = listOf(
                EnrolLastBiometricStepResult.MatchResult(
                    listOf(matchResult(LOW_CONFIDENCE)),
                    Modality.FINGERPRINT,
                    FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                ),
            ),
        )

        assertThat(result).isNull()
    }

    @Test
    fun `returns false if no face matches with high score`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = listOf(
                EnrolLastBiometricStepResult.MatchResult(
                    listOf(matchResult(LOW_CONFIDENCE)),
                    Modality.FACE,
                    FaceConfiguration.BioSdk.RANK_ONE,
                ),
            ),
        )

        assertThat(result).isNull()
    }

    @Test
    fun `returns false if no fingerprint high score threshold`() {
        val result = useCase(
            projectConfig = mockProjectConfig(highConfidence = null),
            steps = listOf(
                EnrolLastBiometricStepResult.MatchResult(
                    listOf(matchResult(HIGH_CONFIDENCE)),
                    Modality.FINGERPRINT,
                    FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                ),
            ),
        )

        assertThat(result).isNull()
    }

    @Test
    fun `returns false if no face high score threshold`() {
        val result = useCase(
            projectConfig = mockProjectConfig(highConfidence = null),
            steps = listOf(
                EnrolLastBiometricStepResult.MatchResult(
                    listOf(matchResult(HIGH_CONFIDENCE)),
                    Modality.FACE,
                    FaceConfiguration.BioSdk.RANK_ONE,
                ),
            ),
        )

        assertThat(result).isNull()
    }

    @Test
    fun `returns true if no previous results provided`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = emptyList(),
        )

        assertThat(result).isEqualTo(EnrolLastState.ErrorType.NO_MATCH_RESULTS)
    }

    @Test
    fun `returns true if there are fingerprint matches with high score`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = listOf(
                EnrolLastBiometricStepResult.MatchResult(
                    listOf(matchResult(HIGH_CONFIDENCE)),
                    Modality.FINGERPRINT,
                    FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                ),
            ),
        )

        assertThat(result).isEqualTo(EnrolLastState.ErrorType.DUPLICATE_ENROLMENTS)
    }

    @Test
    fun `returns true if there are face matches with high score`() {
        val result = useCase(
            projectConfig = mockProjectConfig(),
            steps = listOf(
                EnrolLastBiometricStepResult.MatchResult(
                    listOf(matchResult(HIGH_CONFIDENCE)),
                    Modality.FACE,
                    FaceConfiguration.BioSdk.RANK_ONE,
                ),
            ),
        )

        assertThat(result).isEqualTo(EnrolLastState.ErrorType.DUPLICATE_ENROLMENTS)
    }

    private fun mockProjectConfig(
        checkEnabled: Boolean = true,
        highConfidence: Int? = HIGH_CONFIDENCE_THRESHOLD,
    ): ProjectConfiguration = mockk<ProjectConfiguration>(relaxed = true) {
        every { general.duplicateBiometricEnrolmentCheck } returns checkEnabled

        val stubDecisionPolicy = if (highConfidence != null) {
            DecisionPolicy(0, 0, highConfidence)
        } else {
            null
        }

        every { fingerprint?.secugenSimMatcher?.decisionPolicy } returns stubDecisionPolicy
        every { face?.rankOne?.decisionPolicy } returns stubDecisionPolicy
    }

    private fun matchResult(confidence: Float) = EnrolLastBiometricStepResult.MatchResult.Item("subjectId", confidence)

    companion object {
        private const val LOW_CONFIDENCE = 50f
        private const val HIGH_CONFIDENCE_THRESHOLD = 100
        private const val HIGH_CONFIDENCE = 150f
    }
}
