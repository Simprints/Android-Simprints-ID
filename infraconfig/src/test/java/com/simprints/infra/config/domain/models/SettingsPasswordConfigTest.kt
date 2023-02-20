package com.simprints.infra.config.domain.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettingsPasswordConfigTest {

    @Test
    fun testLocked() {
        assertThat(SettingsPasswordConfig.NotSet.locked).isFalse()
        assertThat(SettingsPasswordConfig.Unlocked.locked).isFalse()
        assertThat(SettingsPasswordConfig.Locked("123").locked).isTrue()
    }
}
