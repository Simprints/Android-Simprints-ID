package com.simprints.infra.config.store.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettingsPasswordConfigTest {
    @Test
    fun testLocked() {
        assertThat(SettingsPasswordConfig.NotSet.locked).isFalse()
        assertThat(SettingsPasswordConfig.Unlocked.locked).isFalse()
        assertThat(SettingsPasswordConfig.Locked("123").locked).isTrue()
    }

    @Test
    fun testGetNullablePassword() {
        assertThat(SettingsPasswordConfig.NotSet.getNullablePassword()).isNull()
        assertThat(SettingsPasswordConfig.Unlocked.getNullablePassword()).isNull()
        assertThat(SettingsPasswordConfig.Locked("123").getNullablePassword()).isNotNull()
    }
}
