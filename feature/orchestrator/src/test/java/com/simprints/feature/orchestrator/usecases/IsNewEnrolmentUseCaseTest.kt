package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.face.matcher.FaceMatchResult
import com.simprints.infra.config.domain.models.DecisionPolicy
import com.simprints.infra.config.domain.models.ProjectConfiguration
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
        every { projectConfiguration.fingerprint?.decisionPolicy } returns fingerprintConfidenceDecisionPolicy

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
    fun `Results are new enrolment if match results are of lower then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(useCase(projectConfiguration, listOf(
            FaceMatchResult(listOf(FaceMatchResult.Item("", lowerThanMediumConfidenceScore))),
        ))).isTrue()
    }

    @Test
    fun `Results are not new enrolment if match results are of higher then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(useCase(projectConfiguration, listOf(
            FaceMatchResult(listOf(FaceMatchResult.Item("", higherThanMediumConfidenceScore))),
        ))).isFalse()
    }

    // TODO add same for fingerprint results and both results

    companion object {
        private const val mediumConfidenceScore = 30
        private const val lowerThanMediumConfidenceScore = mediumConfidenceScore - 1f
        private const val higherThanMediumConfidenceScore = mediumConfidenceScore + 1f
        private val fingerprintConfidenceDecisionPolicy = DecisionPolicy(15, mediumConfidenceScore, 40)
        private val faceConfidenceDecisionPolicy = DecisionPolicy(15, mediumConfidenceScore, 40)
    }
}
