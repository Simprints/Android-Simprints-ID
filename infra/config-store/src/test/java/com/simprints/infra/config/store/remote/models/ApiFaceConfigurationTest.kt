package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.AgeGroup
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
    fun `should map correctly the model with allowedAgeRange present`() {
        val apiFaceConfigurationWithAgeRange = apiFaceConfiguration.copy(
            rankOne = apiFaceConfiguration.rankOne.copy(
                allowedAgeRange = ApiAllowedAgeRange(10, 20),
            ),
        )
        val faceConfigurationWithAgeRange = faceConfiguration.copy(
            rankOne = faceConfiguration.rankOne!!.copy(
                allowedAgeRange = AgeGroup(10, 20),
            ),
        )
        assertThat(apiFaceConfigurationWithAgeRange.toDomain()).isEqualTo(faceConfigurationWithAgeRange)
    }

    @Test
    fun `should map correctly the ImageSavingStrategy enums`() {
        val mapping = mapOf(
            ApiFaceConfiguration.ImageSavingStrategy.NEVER to FaceConfiguration.ImageSavingStrategy.NEVER,
            ApiFaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE to FaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE,
            ApiFaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN to FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should upload images correctly`() {
        val mapping = mapOf(
            FaceConfiguration.ImageSavingStrategy.NEVER to false,
            FaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE to true,
            FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN to true,
        )

        mapping.forEach {
            assertThat(it.key.shouldSaveImage()).isEqualTo(it.value)
        }
    }
}
