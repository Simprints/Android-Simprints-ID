package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.testtools.generalConfiguration
import com.simprints.infra.config.store.testtools.protoGeneralConfiguration
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

class GeneralConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(protoGeneralConfiguration.toDomain()).isEqualTo(generalConfiguration)
        assertThat(generalConfiguration.toProto()).isEqualTo(protoGeneralConfiguration)
    }

    @Test
    fun `should map correctly the Modality enums`() {
        val mapping = mapOf(
            ProtoGeneralConfiguration.Modality.FACE to GeneralConfiguration.Modality.FACE,
            ProtoGeneralConfiguration.Modality.FINGERPRINT to GeneralConfiguration.Modality.FINGERPRINT,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }

    @Test
    fun `should map correctly the settings password`() {
        val mapping = mapOf(
            SettingsPasswordConfig.NotSet to "",
            SettingsPasswordConfig.Locked("123") to "123",
        )

        mapping.forEach { assertThat(it.key.toProto()).isEqualTo(it.value) }
    }

    @Test
    fun `should not allow to save unlocked state`() {
        assertThrows<IllegalStateException> { SettingsPasswordConfig.Unlocked.toProto() }
    }
}
