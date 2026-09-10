package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
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
            "FACE" to Modality.FACE,
            "FINGERPRINT" to Modality.FINGERPRINT,
            "UNKNOWN" to null,
        )

        mapping.forEach { assertThat(ApiGeneralConfiguration.ApiModality.fromString(it.key)).isEqualTo(it.value) }
    }

    @Test
    fun `should map correctly the settings passwords`() {
        val mapping = mapOf(
            null to SettingsPasswordConfig.NotSet,
            "" to SettingsPasswordConfig.NotSet,
            "123" to SettingsPasswordConfig.Locked("123"),
        )

        mapping.forEach { assertThat(SettingsPasswordConfig.toDomain(it.key)).isEqualTo(it.value) }
    }
}
