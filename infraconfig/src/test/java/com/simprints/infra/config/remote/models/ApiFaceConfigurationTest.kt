package com.simprints.infra.config.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.FaceConfiguration
import com.simprints.infra.config.testtools.apiFaceConfiguration
import com.simprints.infra.config.testtools.faceConfiguration
import org.junit.Test

class ApiFaceConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(apiFaceConfiguration.toDomain()).isEqualTo(faceConfiguration)
    }

    @Test
    fun `should map correctly the ImageSavingStrategy enums`() {
        val mapping = mapOf(
            ApiFaceConfiguration.ImageSavingStrategy.NEVER to FaceConfiguration.ImageSavingStrategy.NEVER,
            ApiFaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN to FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should map correctly the ImageSavingStrategy ONLY_GOOD_SCAN`() {
        val imageSavingStrategy = ApiFaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN.toDomain()

        assertThat(imageSavingStrategy).isEqualTo(FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN)
    }
}
