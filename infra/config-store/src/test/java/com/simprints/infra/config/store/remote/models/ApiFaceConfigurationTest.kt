package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.testtools.apiFaceConfiguration
import com.simprints.infra.config.store.testtools.faceConfiguration
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
}
