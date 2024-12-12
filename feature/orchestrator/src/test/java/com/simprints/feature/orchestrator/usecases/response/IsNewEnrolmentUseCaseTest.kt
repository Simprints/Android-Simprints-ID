package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

internal class IsNewEnrolmentUseCaseTest {
    @MockK
    lateinit var projectConfiguration: ProjectConfiguration

    lateinit var useCase: IsNewEnrolmentUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { projectConfiguration.face?.decisionPolicy } returns faceConfidenceDecisionPolicy
        every { projectConfiguration.fingerprint?.getSdkConfiguration((any()))?.decisionPolicy } returns fingerprintConfidenceDecisionPolicy

        useCase = IsNewEnrolmentUseCase()
    }

    @Test
    fun `Results are new enrolment if duplicate check is disabled`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns false

        assertThat(useCase(projectConfiguration, listOf())).isTrue()
    }

    @Test
    fun `Results are new enrolment if there are no match results`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(useCase(projectConfiguration, listOf())).isTrue()
    }

    @Test
    fun `Results are new enrolment if fingerprint match results are of lower then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    FingerprintMatchResult(
                        listOf(FingerprintMatchResult.Item("", LOWER_THAN_MEDIUM_SCORE)),
                        mockk(),
                    ),
                ),
            ),
        ).isTrue()
    }

    @Test
    fun `Results are not new enrolment if fingerprint match results are of higher then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    FingerprintMatchResult(
                        listOf(FingerprintMatchResult.Item("", HIGHER_THAN_MEDIUM_SCORE)),
                        mockk(),
                    ),
                ),
            ),
        ).isFalse()
    }

    @Test
    fun `Results are new enrolment if face match results are of lower then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    FaceMatchResult(listOf(FaceMatchResult.Item("", LOWER_THAN_MEDIUM_SCORE))),
                ),
            ),
        ).isTrue()
    }

    @Test
    fun `Results are not new enrolment if face match results are of higher then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    FaceMatchResult(listOf(FaceMatchResult.Item("", HIGHER_THAN_MEDIUM_SCORE))),
                ),
            ),
        ).isFalse()
    }

    @Test
    fun `Results are new enrolment if all match results are of lower then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    FingerprintMatchResult(
                        listOf(FingerprintMatchResult.Item("", LOWER_THAN_MEDIUM_SCORE)),
                        mockk(),
                    ),
                    FaceMatchResult(listOf(FaceMatchResult.Item("", LOWER_THAN_MEDIUM_SCORE))),
                ),
            ),
        ).isTrue()
    }

    @Test
    fun `Results are not new enrolment if one (fingerprint) match results are of higher then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    FingerprintMatchResult(
                        listOf(FingerprintMatchResult.Item("", LOWER_THAN_MEDIUM_SCORE)),
                        mockk(),
                    ),
                    FaceMatchResult(listOf(FaceMatchResult.Item("", HIGHER_THAN_MEDIUM_SCORE))),
                ),
            ),
        ).isFalse()
    }

    @Test
    fun `Results are not new enrolment if one (face) match results are of higher then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    FingerprintMatchResult(
                        listOf(FingerprintMatchResult.Item("", HIGHER_THAN_MEDIUM_SCORE)),
                        mockk(),
                    ),
                    FaceMatchResult(listOf(FaceMatchResult.Item("", LOWER_THAN_MEDIUM_SCORE))),
                ),
            ),
        ).isFalse()
    }

    companion object {
        private const val MEDIUM_CONFIDENCE_SCORE = 30
        private const val LOWER_THAN_MEDIUM_SCORE = MEDIUM_CONFIDENCE_SCORE - 1f
        private const val HIGHER_THAN_MEDIUM_SCORE = MEDIUM_CONFIDENCE_SCORE + 1f
        private val fingerprintConfidenceDecisionPolicy = DecisionPolicy(15, MEDIUM_CONFIDENCE_SCORE, 40)
        private val faceConfidenceDecisionPolicy = DecisionPolicy(15, MEDIUM_CONFIDENCE_SCORE, 40)
    }
}
