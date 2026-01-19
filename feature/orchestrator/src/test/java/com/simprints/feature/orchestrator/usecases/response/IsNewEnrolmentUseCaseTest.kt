package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.*
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.matching.MatchResult
import io.mockk.*
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

        every { projectConfiguration.face?.rankOne?.decisionPolicy } returns faceConfidenceDecisionPolicy
        every { projectConfiguration.fingerprint?.secugenSimMatcher?.decisionPolicy } returns fingerprintConfidenceDecisionPolicy

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
    fun `Results are new enrolment if all match result is empty`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    MatchResult(
                        listOf(),
                        ModalitySdkType.SECUGEN_SIM_MATCHER,
                    ),
                ),
            ),
        ).isTrue()
    }

    @Test
    fun `Results are new enrolment if fingerprint match results are of lower then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    MatchResult(
                        listOf(ComparisonResult("", LOWER_THAN_MEDIUM_SCORE)),
                        ModalitySdkType.SECUGEN_SIM_MATCHER,
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
                    MatchResult(
                        listOf(ComparisonResult("", HIGHER_THAN_MEDIUM_SCORE)),
                        ModalitySdkType.SECUGEN_SIM_MATCHER,
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
                listOf(MatchResult(listOf(ComparisonResult("", LOWER_THAN_MEDIUM_SCORE)), ModalitySdkType.RANK_ONE)),
            ),
        ).isTrue()
    }

    @Test
    fun `Results are not new enrolment if face match results are of higher then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(MatchResult(listOf(ComparisonResult("", HIGHER_THAN_MEDIUM_SCORE)), ModalitySdkType.RANK_ONE)),
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
                    MatchResult(
                        listOf(ComparisonResult("", LOWER_THAN_MEDIUM_SCORE)),
                        ModalitySdkType.SECUGEN_SIM_MATCHER,
                    ),
                    MatchResult(listOf(ComparisonResult("", LOWER_THAN_MEDIUM_SCORE)), ModalitySdkType.RANK_ONE),
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
                    MatchResult(
                        listOf(ComparisonResult("", LOWER_THAN_MEDIUM_SCORE)),
                        ModalitySdkType.SECUGEN_SIM_MATCHER,
                    ),
                    MatchResult(listOf(ComparisonResult("", HIGHER_THAN_MEDIUM_SCORE)), ModalitySdkType.RANK_ONE),
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
                    MatchResult(
                        listOf(ComparisonResult("", HIGHER_THAN_MEDIUM_SCORE)),
                        ModalitySdkType.SECUGEN_SIM_MATCHER,
                    ),
                    MatchResult(listOf(ComparisonResult("", LOWER_THAN_MEDIUM_SCORE)), ModalitySdkType.RANK_ONE),
                ),
            ),
        ).isFalse()
    }

    @Test
    fun `Results are not new enrolment if external credential match results exist`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        val credentialMatches = listOf<CredentialMatch>(
            mockk {
                every { comparisonResult } returns mockk {
                    every { subjectId } returns "subjectId"
                    every { comparisonScore } returns 50f
                }
                every { bioSdk } returns ModalitySdkType.RANK_ONE
            },
        )

        assertThat(
            useCase(
                projectConfiguration,
                listOf(
                    mockk<ExternalCredentialSearchResult> {
                        every { matchResults } returns credentialMatches
                    },
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
