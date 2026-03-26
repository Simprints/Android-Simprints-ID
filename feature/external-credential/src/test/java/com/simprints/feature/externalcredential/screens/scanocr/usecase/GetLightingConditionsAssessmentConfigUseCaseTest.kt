package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessmentConfig
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING
import com.simprints.infra.config.store.models.ProjectConfiguration
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetLightingConditionsAssessmentConfigUseCaseTest {
    @MockK
    private lateinit var configRepository: ConfigRepository

    private lateinit var useCase: GetLightingConditionsAssessmentConfigUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetLightingConditionsAssessmentConfigUseCase(configRepository)
    }

    @Test
    fun `returns null when lighting conditions assessment is disabled`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration(
            customConfig = mapOf(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED to JsonPrimitive(false),
            ),
        )

        assertThat(useCase()).isNull()
    }

    @Test
    fun `returns experimental lighting config when lighting conditions assessment is enabled`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration(
            customConfig = mapOf(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED to JsonPrimitive(true),
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING to JsonPrimitive(7),
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST to JsonPrimitive(31),
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS to JsonPrimitive(21),
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS to JsonPrimitive(91),
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS to JsonPrimitive(97),
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY to JsonPrimitive(8),
            ),
        )

        assertThat(useCase()).isEqualTo(
            LightingConditionsAssessmentConfig(
                isEnabled = true,
                borderWidthPercent = 7,
                lowContrastThresholdPercent = 31,
                lowMedianLuminanceThresholdPercent = 21,
                highMedianLuminanceThresholdPercent = 91,
                highGlareLuminanceThresholdPercent = 97,
                glareDetectionGridMinDimension = 8,
            ),
        )
    }

    private fun projectConfiguration(customConfig: Map<String, JsonPrimitive>) = mockk<ProjectConfiguration> {
        every { custom } returns customConfig
    }
}
