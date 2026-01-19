package com.simprints.feature.externalcredential.screens.search.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getSdkListForAgeGroup
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
    private lateinit var faceCapture: BiometricReferenceCapture

    @MockK
    private lateinit var fingerprintCapture: BiometricReferenceCapture

    @MockK
    private lateinit var generalConfiguration: GeneralConfiguration

    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")

        every { faceCapture.modality } returns Modality.FACE
        every { fingerprintCapture.modality } returns Modality.FINGERPRINT

        every { projectConfiguration.general } returns generalConfiguration
    }

    @Test
    fun `creates correct MatchParams for face modality`() {
        every { projectConfiguration.getSdkListForAgeGroup(Modality.FACE, ageGroup) } returns listOf(
            ModalitySdkType.RANK_ONE,
            ModalitySdkType.SIM_FACE,
        )
        every { generalConfiguration.matchingModalities } returns listOf(Modality.FACE)

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            projectConfiguration = projectConfiguration,
            probeReferences = listOf(faceCapture),
            ageGroup = ageGroup,
        )

        assertThat(result).hasSize(2)
        result.forEach { matchParams ->
            assertThat(matchParams.flowType).isEqualTo(flowType)
            assertThat(matchParams.queryForCandidates.subjectId).isEqualTo(subjectId)
            assertThat(matchParams.biometricDataSource).isEqualTo(BiometricDataSource.Simprints)
            assertThat(matchParams.probeReference).isEqualTo(faceCapture)
            assertThat(matchParams.bioSdk).isNotNull()
        }
        assertThat(result[0].bioSdk).isEqualTo(ModalitySdkType.RANK_ONE)
        assertThat(result[1].bioSdk).isEqualTo(ModalitySdkType.SIM_FACE)
    }

    @Test
    fun `creates correct  MatchParams for fingerprint modality`() {
        every { projectConfiguration.getSdkListForAgeGroup(Modality.FINGERPRINT, ageGroup) } returns listOf(
            ModalitySdkType.SECUGEN_SIM_MATCHER,
            ModalitySdkType.NEC,
        )
        every { generalConfiguration.matchingModalities } returns listOf(Modality.FINGERPRINT)

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            projectConfiguration = projectConfiguration,
            probeReferences = listOf(fingerprintCapture),
            ageGroup = ageGroup,
        )

        assertThat(result).hasSize(2)
        result.forEach { matchParams ->
            assertThat(matchParams.flowType).isEqualTo(flowType)
            assertThat(matchParams.queryForCandidates.subjectId).isEqualTo(subjectId)
            assertThat(matchParams.biometricDataSource).isEqualTo(BiometricDataSource.Simprints)
            assertThat(matchParams.probeReference).isEqualTo(fingerprintCapture)
            assertThat(matchParams.bioSdk).isNotNull()
        }
        assertThat(result[0].bioSdk).isEqualTo(ModalitySdkType.SECUGEN_SIM_MATCHER)
        assertThat(result[1].bioSdk).isEqualTo(ModalitySdkType.NEC)
    }

    @Test
    fun `creates correct  MatchParams for multiple`() {
        every { generalConfiguration.matchingModalities } returns listOf(
            Modality.FACE,
            Modality.FINGERPRINT,
        )
        every { projectConfiguration.getSdkListForAgeGroup(Modality.FACE, ageGroup) } returns listOf(
            ModalitySdkType.RANK_ONE,
        )
        every { projectConfiguration.getSdkListForAgeGroup(Modality.FINGERPRINT, ageGroup) } returns listOf(
            ModalitySdkType.NEC,
        )

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            projectConfiguration = projectConfiguration,
            probeReferences = listOf(fingerprintCapture, faceCapture),
            ageGroup = ageGroup,
        )

        assertThat(result).hasSize(2)

        val faceMatch = result.find { it.bioSdk.modality() == Modality.FACE }
        assertThat(faceMatch).isNotNull()
        assertThat(faceMatch?.bioSdk).isEqualTo(ModalitySdkType.RANK_ONE)
        assertThat(faceMatch?.probeReference).isEqualTo(faceCapture)

        val fingerprintMatch = result.find { it.bioSdk.modality() == Modality.FINGERPRINT }
        assertThat(fingerprintMatch).isNotNull()
        assertThat(fingerprintMatch?.bioSdk).isEqualTo(ModalitySdkType.NEC)
        assertThat(fingerprintMatch?.probeReference).isEqualTo(fingerprintCapture)
    }

    @Test
    fun `handles null ageGroup`() {
        every { projectConfiguration.getSdkListForAgeGroup(Modality.FACE, null) } returns listOf(
            ModalitySdkType.RANK_ONE,
        )
        every { generalConfiguration.matchingModalities } returns listOf(Modality.FACE)

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            projectConfiguration = projectConfiguration,
            probeReferences = listOf(faceCapture),
            ageGroup = null,
        )

        assertThat(result).hasSize(1)
    }

    @Test
    fun `returns empty list when no SDKs available`() {
        every { projectConfiguration.getSdkListForAgeGroup(any(), ageGroup) } returns emptyList()

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            projectConfiguration = projectConfiguration,
            probeReferences = listOf(fingerprintCapture, faceCapture),
            ageGroup = ageGroup,
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `creates multiple MatchParams for multiple face SDKs`() {
        every { projectConfiguration.getSdkListForAgeGroup(Modality.FACE, ageGroup) } returns listOf(
            ModalitySdkType.RANK_ONE,
            ModalitySdkType.SIM_FACE,
        )
        every { generalConfiguration.matchingModalities } returns listOf(Modality.FACE)

        val faceSamples = listOf(
            faceCapture,
            mockk { every { modality } returns Modality.FACE },
        )

        val result = useCase(
            candidateSubjectId = subjectId,
            flowType = flowType,
            projectConfiguration = projectConfiguration,
            probeReferences = faceSamples,
            ageGroup = ageGroup,
        )

        // 2 captures * 2 sdks
        assertThat(result).hasSize(4)
    }
}
