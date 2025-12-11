package com.simprints.feature.externalcredential.screens.search.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.sample.MatchComparisonResult
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalitySdkConfig
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.matching.MatchParams
import com.simprints.infra.matching.usecase.FaceMatcherUseCase
import com.simprints.infra.matching.usecase.FingerprintMatcherUseCase
import com.simprints.infra.matching.usecase.MatcherUseCase.MatcherState
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class MatchCandidatesUseCaseTest {
    private lateinit var useCase: MatchCandidatesUseCase

    @MockK
    private lateinit var createMatchParamsUseCase: CreateMatchParamsUseCase

    @MockK
    private lateinit var faceMatcher: FaceMatcherUseCase

    @MockK
    private lateinit var fingerprintMatcher: FingerprintMatcherUseCase

    @MockK
    private lateinit var subject: Subject

    @MockK
    private lateinit var project: Project

    @MockK
    private lateinit var projectConfig: ProjectConfiguration

    @MockK
    private lateinit var externalCredentialParams: ExternalCredentialParams

    @MockK
    private lateinit var faceConfig: FaceConfiguration

    @MockK
    private lateinit var fingerprintConfig: FingerprintConfiguration

    @MockK
    private lateinit var faceSdkConfig: FaceConfiguration.FaceSdkConfiguration

    @MockK
    private lateinit var fingerprintSdkConfig: FingerprintConfiguration.FingerprintSdkConfiguration

    @MockK
    private lateinit var matchResultItem: MatchComparisonResult

    @MockK
    private lateinit var matchParams: MatchParams

    @MockK
    private lateinit var faceCapture: BiometricReferenceCapture

    @MockK
    private lateinit var fingerprintCapture: BiometricReferenceCapture

    @MockK
    private lateinit var ageGroup: AgeGroup

    @MockK
    private lateinit var matcherSuccess: MatcherState.Success

    private val credential = "credential".asTokenizableEncrypted()
    private val subjectId = "subjectId"
    private val probeReferenceId = "probeReferenceId"
    private val verificationMatchThreshold = 50.0f

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = MatchCandidatesUseCase(
            createMatchParamsUseCase = createMatchParamsUseCase,
            faceMatcher = faceMatcher,
            fingerprintMatcher = fingerprintMatcher,
        )

        every { subject.subjectId } returns subjectId
        every { externalCredentialParams.flowType } returns FlowType.VERIFY
        every { externalCredentialParams.probeReferences } returns listOf(faceCapture, fingerprintCapture)
        every { externalCredentialParams.ageGroup } returns ageGroup

        coEvery {
            createMatchParamsUseCase(
                candidateSubjectId = any(),
                flowType = any(),
                projectConfiguration = any(),
                probeReferences = any(),
                ageGroup = any(),
            )
        } returns listOf(matchParams)
        every { projectConfig.face } returns faceConfig
        every { projectConfig.fingerprint } returns fingerprintConfig
        every { projectConfig.getModalitySdkConfig(FaceConfiguration.BioSdk.RANK_ONE) } returns faceSdkConfig
        every { faceSdkConfig.verificationMatchThreshold } returns verificationMatchThreshold
        every { projectConfig.getModalitySdkConfig(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER) } returns fingerprintSdkConfig
        every { fingerprintSdkConfig.verificationMatchThreshold } returns verificationMatchThreshold
        every { matcherSuccess.comparisonResults } returns listOf(matchResultItem)
        coEvery { faceMatcher(matchParams, project) } returns flowOf(matcherSuccess)
        coEvery { fingerprintMatcher(matchParams, project) } returns flowOf(matcherSuccess)
    }

    private fun initMatchParams(isFace: Boolean) {
        if (isFace) {
            every { matchParams.probeReference } returns faceCapture
            every { matchParams.bioSdk } returns FaceConfiguration.BioSdk.RANK_ONE
        } else {
            every { matchParams.probeReference } returns fingerprintCapture
            every { matchParams.bioSdk } returns FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
        }
    }

    @Test
    fun `returns face matches when face samples present`() = runTest {
        initMatchParams(isFace = true)
        val result = useCase.invoke(
            candidates = listOf(subject),
            credential = credential,
            externalCredentialParams = externalCredentialParams,
            project = project,
            projectConfig = projectConfig,
        )

        assertThat(result).hasSize(1)
        assertThat(result[0].credential).isEqualTo(credential)
        assertThat(result[0].matchResult).isEqualTo(matchResultItem)
        assertThat(result[0].verificationThreshold).isEqualTo(verificationMatchThreshold)
        assertThat(result[0].bioSdk).isEqualTo(FaceConfiguration.BioSdk.RANK_ONE)
    }

    @Test
    fun `returns fingerprint matches when no face samples present`() = runTest {
        initMatchParams(isFace = false)
        val result = useCase.invoke(
            candidates = listOf(subject),
            credential = credential,
            externalCredentialParams = externalCredentialParams,
            project = project,
            projectConfig = projectConfig,
        )

        assertThat(result).hasSize(1)
        assertThat(result[0].credential).isEqualTo(credential)
        assertThat(result[0].matchResult).isEqualTo(matchResultItem)
        assertThat(result[0].verificationThreshold).isEqualTo(verificationMatchThreshold)
        assertThat(result[0].bioSdk).isEqualTo(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)
    }

    @Test
    fun `returns empty list when no candidates provided`() = runTest {
        val result = useCase.invoke(
            candidates = emptyList(),
            credential = credential,
            externalCredentialParams = externalCredentialParams,
            project = project,
            projectConfig = projectConfig,
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `returns empty list when face SDK configuration is null`() = runTest {
        initMatchParams(isFace = true)
        every { projectConfig.getModalitySdkConfig(FaceConfiguration.BioSdk.RANK_ONE) } returns null

        val result = useCase.invoke(
            candidates = listOf(subject),
            credential = credential,
            externalCredentialParams = externalCredentialParams,
            project = project,
            projectConfig = projectConfig,
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `returns empty list when fingerprint SDK configuration is null`() = runTest {
        initMatchParams(isFace = false)
        every { projectConfig.getModalitySdkConfig(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER) } returns null

        val result = useCase.invoke(
            candidates = listOf(subject),
            credential = credential,
            externalCredentialParams = externalCredentialParams,
            project = project,
            projectConfig = projectConfig,
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `returns empty list when face verification match threshold is null`() = runTest {
        initMatchParams(isFace = true)
        every { faceSdkConfig.verificationMatchThreshold } returns null
        val result = useCase(
            candidates = listOf(subject),
            credential = credential,
            externalCredentialParams = externalCredentialParams,
            project = project,
            projectConfig = projectConfig,
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `returns empty list when fingerprint match threshold is null`() = runTest {
        initMatchParams(isFace = false)
        every { fingerprintSdkConfig.verificationMatchThreshold } returns null
        val result = useCase(
            candidates = listOf(subject),
            credential = credential,
            externalCredentialParams = externalCredentialParams,
            project = project,
            projectConfig = projectConfig,
        )
        assertThat(result).isEmpty()
    }
}
