package com.simprints.feature.externalcredential.screens.search.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.determineFaceSDKs
import com.simprints.infra.config.store.models.determineFingerprintSDKs
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

internal class CreateMatchParamsUseCaseTest {
    private val useCase = CreateMatchParamsUseCase()

    private val subjectId = "subjectId"
    private val probeReferenceId = "probeReferenceId"
    private val flowType = FlowType.IDENTIFY
    private val ageGroup = AgeGroup(25, 30)

    @MockK
    private lateinit var faceSample: CaptureSample

    @MockK
    private lateinit var fingerprintSample: CaptureSample

    @MockK
    private lateinit var generalConfiguration: GeneralConfiguration

    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
        every { projectConfiguration.general } returns generalConfiguration
    }

    @Test
    fun `creates correct MatchParams for face modality`() {
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns listOf(
            FaceConfiguration.BioSdk.RANK_ONE,
            FaceConfiguration.BioSdk.SIM_FACE,
        )
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns emptyList()
        every { generalConfiguration.matchingModalities } returns listOf(Modality.FACE)

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = listOf(faceSample),
            fingerprintSamples = emptyList(),
            ageGroup = ageGroup,
        )

        assertThat(result).hasSize(2)
        result.forEach { matchParams ->
            assertThat(matchParams.probeReferenceId).isEqualTo(probeReferenceId)
            assertThat(matchParams.flowType).isEqualTo(flowType)
            assertThat(matchParams.queryForCandidates.subjectId).isEqualTo(subjectId)
            assertThat(matchParams.biometricDataSource).isEqualTo(BiometricDataSource.Simprints)
            assertThat(matchParams.probeSamples).containsExactly(faceSample)
            assertThat(matchParams.bioSdk).isNotNull()
        }
        assertThat(result[0].bioSdk).isEqualTo(FaceConfiguration.BioSdk.RANK_ONE)
        assertThat(result[1].bioSdk).isEqualTo(FaceConfiguration.BioSdk.SIM_FACE)
    }

    @Test
    fun `creates correct  MatchParams for fingerprint modality`() {
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns emptyList()
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns listOf(
            FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
            FingerprintConfiguration.BioSdk.NEC,
        )
        every { generalConfiguration.matchingModalities } returns listOf(Modality.FINGERPRINT)

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = emptyList(),
            fingerprintSamples = listOf(fingerprintSample),
            ageGroup = ageGroup,
        )

        assertThat(result).hasSize(2)
        result.forEach { matchParams ->
            assertThat(matchParams.probeReferenceId).isEqualTo(probeReferenceId)
            assertThat(matchParams.flowType).isEqualTo(flowType)
            assertThat(matchParams.queryForCandidates.subjectId).isEqualTo(subjectId)
            assertThat(matchParams.biometricDataSource).isEqualTo(BiometricDataSource.Simprints)
            assertThat(matchParams.probeSamples).containsExactly(fingerprintSample)
            assertThat(matchParams.bioSdk).isNotNull()
        }
        assertThat(result[0].bioSdk).isEqualTo(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)
        assertThat(result[1].bioSdk).isEqualTo(FingerprintConfiguration.BioSdk.NEC)
    }

    @Test
    fun `creates correct  MatchParams for multiple`() {
        every { generalConfiguration.matchingModalities } returns listOf(
            Modality.FACE,
            Modality.FINGERPRINT,
        )
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns listOf(
            FaceConfiguration.BioSdk.RANK_ONE,
        )
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns listOf(
            FingerprintConfiguration.BioSdk.NEC,
        )

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = listOf(faceSample),
            fingerprintSamples = listOf(fingerprintSample),
            ageGroup = ageGroup,
        )

        assertThat(result).hasSize(2)

        val faceMatch = result.find { it.bioSdk is FaceConfiguration.BioSdk }
        assertThat(faceMatch).isNotNull()
        assertThat(faceMatch?.bioSdk).isEqualTo(FaceConfiguration.BioSdk.RANK_ONE)
        assertThat(faceMatch?.probeSamples).containsExactly(faceSample)

        val fingerprintMatch = result.find { it.bioSdk is FingerprintConfiguration.BioSdk }
        assertThat(fingerprintMatch).isNotNull()
        assertThat(fingerprintMatch?.bioSdk).isEqualTo(FingerprintConfiguration.BioSdk.NEC)
        assertThat(fingerprintMatch?.probeSamples).containsExactly(fingerprintSample)
    }

    @Test
    fun `handles null ageGroup`() {
        every { projectConfiguration.determineFaceSDKs(null) } returns listOf(
            FaceConfiguration.BioSdk.RANK_ONE,
        )
        every { projectConfiguration.determineFingerprintSDKs(null) } returns emptyList()
        every { generalConfiguration.matchingModalities } returns listOf(Modality.FACE)

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = listOf(faceSample),
            fingerprintSamples = emptyList(),
            ageGroup = null,
        )

        assertThat(result).hasSize(1)
    }

    @Test
    fun `returns empty list when no SDKs available`() {
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns emptyList()
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns emptyList()

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = listOf(faceSample),
            fingerprintSamples = listOf(fingerprintSample),
            ageGroup = ageGroup,
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `creates multiple MatchParams for multiple face SDKs`() {
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns listOf(
            FaceConfiguration.BioSdk.RANK_ONE,
            FaceConfiguration.BioSdk.SIM_FACE,
        )
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns emptyList()
        every { generalConfiguration.matchingModalities } returns listOf(Modality.FACE)

        val faceSamples = listOf(faceSample, mockk(relaxed = true))

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = faceSamples,
            fingerprintSamples = emptyList(),
            ageGroup = ageGroup,
        )

        assertThat(result).hasSize(2)
        result.forEach { matchParams ->
            assertThat(matchParams.probeSamples).hasSize(2)
        }
    }
}
