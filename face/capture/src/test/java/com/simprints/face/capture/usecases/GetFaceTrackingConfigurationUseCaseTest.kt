package com.simprints.face.capture.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ProjectConfiguration
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test

internal class GetFaceTrackingConfigurationUseCaseTest {
    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    private val useCase = GetFaceTrackingConfigurationUseCase()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    /**
     * Keys are spelled out rather than imported, since they are internal to the config store. That
     * also pins the names the backend actually sends.
     */
    private fun customConfig(vararg entries: Pair<String, JsonElement>) {
        every { projectConfiguration.custom } returns mapOf(*entries)
    }

    @Test
    fun `tracking is off and the sizes take their defaults when nothing is configured`() {
        customConfig()

        with(useCase(projectConfiguration)) {
            assertThat(enabled).isFalse()
            assertThat(minFaceSizePx).isEqualTo(150)
            assertThat(maxImageSizePx).isEqualTo(300)
        }
    }

    @Test
    fun `configured sizes are carried through`() {
        customConfig(
            "faceTrackingCaptureEnabled" to JsonPrimitive(true),
            "faceTrackingMinFaceSizePx" to JsonPrimitive(200),
            "faceTrackingMaxImageSizePx" to JsonPrimitive(600),
        )

        with(useCase(projectConfiguration)) {
            assertThat(enabled).isTrue()
            assertThat(minFaceSizePx).isEqualTo(200)
            assertThat(maxImageSizePx).isEqualTo(600)
        }
    }

    @Test
    fun `a cap configured below the floor is raised to it rather than left unusable`() {
        customConfig(
            "faceTrackingMinFaceSizePx" to JsonPrimitive(400),
            "faceTrackingMaxImageSizePx" to JsonPrimitive(200),
        )

        // Scaling an accepted face down to 200px would put every capture back under the floor
        with(useCase(projectConfiguration)) {
            assertThat(minFaceSizePx).isEqualTo(400)
            assertThat(maxImageSizePx).isEqualTo(400)
        }
    }

    @Test
    fun `the sizes are resolved even while tracking is off, so they are never read unset`() {
        customConfig("faceTrackingMinFaceSizePx" to JsonPrimitive(250))

        with(useCase(projectConfiguration)) {
            assertThat(enabled).isFalse()
            assertThat(minFaceSizePx).isEqualTo(250)
        }
    }
}
