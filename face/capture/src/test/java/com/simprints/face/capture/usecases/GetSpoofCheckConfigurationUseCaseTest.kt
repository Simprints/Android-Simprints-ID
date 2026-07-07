package com.simprints.face.capture.usecases

import com.google.common.truth.Truth.*
import com.simprints.face.infra.biosdkresolver.RocV3BioSdk
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration.SpoofCheckConfiguration
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.config.store.models.ProjectConfiguration
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test

class GetSpoofCheckConfigurationUseCaseTest {
    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    private lateinit var faceConfiguration: FaceConfiguration

    @MockK
    private lateinit var faceSdkConfiguration: FaceConfiguration.FaceSdkConfiguration

    @MockK
    private lateinit var rocV3BioSdk: RocV3BioSdk

    private lateinit var useCase: GetSpoofCheckConfigurationUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { rocV3BioSdk.version() } returns "3.1"

        useCase = GetSpoofCheckConfigurationUseCase(rocV3BioSdk)
    }

    @Test
    fun `Returns disabled when face is null`() {
        every { projectConfiguration.face } returns null
        val result = useCase(projectConfiguration, ModalitySdkType.RANK_ONE)

        assertThat(result).isEqualTo(SpoofCheckConfiguration.DISABLED)
    }

    @Test
    fun `Returns disabled when face sdk config is not ROC`() {
        val result = useCase(projectConfiguration, ModalitySdkType.SIM_FACE)

        assertThat(result).isEqualTo(SpoofCheckConfiguration.DISABLED)
    }

    @Test
    fun `Returns disabled when face sdk config is not ROC V3`() {
        every { projectConfiguration.face } returns faceConfiguration
        every { faceConfiguration.getSdkConfiguration(any()) } returns faceSdkConfiguration
        every { faceSdkConfiguration.version } returns "1.0"

        val result = useCase(projectConfiguration, ModalitySdkType.RANK_ONE)

        assertThat(result).isEqualTo(SpoofCheckConfiguration.DISABLED)
    }

    @Test
    fun `Returns config when ROC V3 is used`() {
        every { projectConfiguration.face } returns faceConfiguration
        every { faceConfiguration.getSdkConfiguration(any()) } returns faceSdkConfiguration
        every { faceSdkConfiguration.version } returns "3.1"
        every { projectConfiguration.custom } returns mapOf(
            "spoofCheckMode" to JsonPrimitive("ENFORCED"),
            "spoofCheckThreshold" to JsonPrimitive(0.75f),
            "spoofMaxAttempts" to JsonPrimitive(3),
            "spoofMaxBitmapSize" to JsonPrimitive(1000),
        )

        val result = useCase(projectConfiguration, ModalitySdkType.RANK_ONE)

        assertThat(result.mode).isEqualTo(FaceConfiguration.SpoofCheckMode.ENFORCED)
        assertThat(result.threshold).isEqualTo(0.75f)
        assertThat(result.maxAttempts).isEqualTo(3)
        assertThat(result.maxBitmapSize).isEqualTo(1000)
    }
}
