package com.simprints.feature.externalcredential.screens.search.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.FlowType
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.determineFaceSDKs
import com.simprints.infra.config.store.models.determineFingerprintSDKs
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.matching.MatchParams
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
    private lateinit var faceSample: MatchParams.FaceSample

    @MockK
    private lateinit var fingerprintSample: MatchParams.FingerprintSample

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
    fun ` creates correct MatchParams for face modality`() {
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns listOf(
            FaceConfiguration.BioSdk.RANK_ONE,
            FaceConfiguration.BioSdk.SIM_FACE
        )
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns emptyList()
        every { generalConfiguration.matchingModalities } returns listOf(GeneralConfiguration.Modality.FACE)

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = listOf(faceSample),
            fingerprintSamples = emptyList(),
            ageGroup = ageGroup
        )

        assertThat(result).hasSize(2)
        result.forEach { matchParams ->
            assertThat(matchParams.probeReferenceId).isEqualTo(probeReferenceId)
            assertThat(matchParams.flowType).isEqualTo(flowType)
            assertThat(matchParams.queryForCandidates.subjectId).isEqualTo(subjectId)
            assertThat(matchParams.biometricDataSource).isEqualTo(BiometricDataSource.Simprints)
            assertThat(matchParams.probeFaceSamples).containsExactly(faceSample)
            assertThat(matchParams.faceSDK).isNotNull()
        }
        assertThat(result[0].faceSDK).isEqualTo(FaceConfiguration.BioSdk.RANK_ONE)
        assertThat(result[1].faceSDK).isEqualTo(FaceConfiguration.BioSdk.SIM_FACE)
    }

    @Test
    fun ` creates correct  MatchParams for fingerprint modality`() {
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns emptyList()
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns listOf(
            FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
            FingerprintConfiguration.BioSdk.NEC
        )
        every { generalConfiguration.matchingModalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = emptyList(),
            fingerprintSamples = listOf(fingerprintSample),
            ageGroup = ageGroup
        )

        assertThat(result).hasSize(2)
        result.forEach { matchParams ->
            assertThat(matchParams.probeReferenceId).isEqualTo(probeReferenceId)
            assertThat(matchParams.flowType).isEqualTo(flowType)
            assertThat(matchParams.queryForCandidates.subjectId).isEqualTo(subjectId)
            assertThat(matchParams.biometricDataSource).isEqualTo(BiometricDataSource.Simprints)
            assertThat(matchParams.probeFingerprintSamples).containsExactly(fingerprintSample)
            assertThat(matchParams.fingerprintSDK).isNotNull()
        }
        assertThat(result[0].fingerprintSDK).isEqualTo(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)
        assertThat(result[1].fingerprintSDK).isEqualTo(FingerprintConfiguration.BioSdk.NEC)
    }

    @Test
    fun ` creates correct  MatchParams for multiple`() {
        every { generalConfiguration.matchingModalities } returns listOf(
            GeneralConfiguration.Modality.FACE,
            GeneralConfiguration.Modality.FINGERPRINT
        )
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns listOf(
            FaceConfiguration.BioSdk.RANK_ONE
        )
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns listOf(
            FingerprintConfiguration.BioSdk.NEC
        )

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = listOf(faceSample),
            fingerprintSamples = listOf(fingerprintSample),
            ageGroup = ageGroup
        )

        assertThat(result).hasSize(2)

        val faceMatch = result.find { it.faceSDK != null }
        assertThat(faceMatch).isNotNull()
        assertThat(faceMatch?.faceSDK).isEqualTo(FaceConfiguration.BioSdk.RANK_ONE)
        assertThat(faceMatch?.probeFaceSamples).containsExactly(faceSample)

        val fingerprintMatch = result.find { it.fingerprintSDK != null }
        assertThat(fingerprintMatch).isNotNull()
        assertThat(fingerprintMatch?.fingerprintSDK).isEqualTo(FingerprintConfiguration.BioSdk.NEC)
        assertThat(fingerprintMatch?.probeFingerprintSamples).containsExactly(fingerprintSample)
    }

    @Test
    fun ` handles null ageGroup`() {
        every { projectConfiguration.determineFaceSDKs(null) } returns listOf(
            FaceConfiguration.BioSdk.RANK_ONE
        )
        every { projectConfiguration.determineFingerprintSDKs(null) } returns emptyList()
        every { generalConfiguration.matchingModalities } returns listOf(GeneralConfiguration.Modality.FACE)

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = listOf(faceSample),
            fingerprintSamples = emptyList(),
            ageGroup = null
        )

        assertThat(result).hasSize(1)
    }

    @Test
    fun ` returns empty list when no SDKs available`() {
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns emptyList()
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns emptyList()

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = listOf(faceSample),
            fingerprintSamples = listOf(fingerprintSample),
            ageGroup = ageGroup
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun ` creates multiple MatchParams for multiple face SDKs`() {
        every { projectConfiguration.determineFaceSDKs(ageGroup) } returns listOf(
            FaceConfiguration.BioSdk.RANK_ONE,
            FaceConfiguration.BioSdk.SIM_FACE
        )
        every { projectConfiguration.determineFingerprintSDKs(ageGroup) } returns emptyList()
        every { generalConfiguration.matchingModalities } returns listOf(GeneralConfiguration.Modality.FACE)

        val faceSamples = listOf(faceSample, mockk(relaxed = true))

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            probeReferenceId = probeReferenceId,
            projectConfiguration = projectConfiguration,
            faceSamples = faceSamples,
            fingerprintSamples = emptyList(),
            ageGroup = ageGroup
        )

        assertThat(result).hasSize(2)
        result.forEach { matchParams ->
            assertThat(matchParams.probeFaceSamples).hasSize(2)
        }
    }
}
