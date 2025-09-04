package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.*
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.matching.FaceMatchResult
import com.simprints.infra.matching.FingerprintMatchResult
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

        every { projectConfiguration.face?.getSdkConfiguration((any()))?.decisionPolicy } returns faceConfidenceDecisionPolicy
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
                listOf(FingerprintMatchResult(listOf(MatchConfidence("", LOWER_THAN_MEDIUM_SCORE)), mockk())),
            ),
        ).isTrue()
    }

    @Test
    fun `Results are not new enrolment if fingerprint match results are of higher then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(FingerprintMatchResult(listOf(MatchConfidence("", HIGHER_THAN_MEDIUM_SCORE)), mockk())),
            ),
        ).isFalse()
    }

    @Test
    fun `Results are new enrolment if face match results are of lower then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(FaceMatchResult(listOf(MatchConfidence("", LOWER_THAN_MEDIUM_SCORE)), FaceConfiguration.BioSdk.RANK_ONE)),
            ),
        ).isTrue()
    }

    @Test
    fun `Results are not new enrolment if face match results are of higher then medium confidence`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        assertThat(
            useCase(
                projectConfiguration,
                listOf(FaceMatchResult(listOf(MatchConfidence("", HIGHER_THAN_MEDIUM_SCORE)), FaceConfiguration.BioSdk.RANK_ONE)),
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
                        listOf(MatchConfidence("", LOWER_THAN_MEDIUM_SCORE)),
                        mockk(),
                    ),
                    FaceMatchResult(listOf(MatchConfidence("", LOWER_THAN_MEDIUM_SCORE)), FaceConfiguration.BioSdk.RANK_ONE),
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
                        listOf(MatchConfidence("", LOWER_THAN_MEDIUM_SCORE)),
                        mockk(),
                    ),
                    FaceMatchResult(listOf(MatchConfidence("", HIGHER_THAN_MEDIUM_SCORE)), FaceConfiguration.BioSdk.RANK_ONE),
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
                        listOf(MatchConfidence("", HIGHER_THAN_MEDIUM_SCORE)),
                        mockk(),
                    ),
                    FaceMatchResult(listOf(MatchConfidence("", LOWER_THAN_MEDIUM_SCORE)), FaceConfiguration.BioSdk.RANK_ONE),
                ),
            ),
        ).isFalse()
    }

    @Test
    fun `Results are not new enrolment if external credential match results exist`() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        val credentialMatches = listOf<CredentialMatch>(
            mockk {
                every { matchResult } returns mockk {
                    every { subjectId } returns "subjectId"
                    every { confidence } returns 50f
                }
                every { faceBioSdk } returns FaceConfiguration.BioSdk.RANK_ONE
                every { fingerprintBioSdk } returns null
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
