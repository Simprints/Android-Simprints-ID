package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.testtools.apiGeneralConfiguration
import com.simprints.infra.config.store.testtools.generalConfiguration
import org.junit.Test

class ApiGeneralConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(apiGeneralConfiguration.toDomain()).isEqualTo(generalConfiguration)
    }

    @Test
    fun `should map correctly the Modality enums`() {
        val mapping = mapOf(
            ApiGeneralConfiguration.Modality.FACE to GeneralConfiguration.Modality.FACE,
            ApiGeneralConfiguration.Modality.FINGERPRINT to GeneralConfiguration.Modality.FINGERPRINT,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should map correctly the settings passwords`() {
        assertThat(SettingsPasswordConfig.toDomain(null)).isEqualTo(
            SettingsPasswordConfig.NotSet,
        )
        assertThat(SettingsPasswordConfig.toDomain("")).isEqualTo(
            SettingsPasswordConfig.NotSet,
        )
        assertThat(SettingsPasswordConfig.toDomain("123")).isEqualTo(
            SettingsPasswordConfig.Locked("123"),
        )
    }
}
