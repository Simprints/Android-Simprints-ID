package com.simprints.id.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CosyncSettingTest {

    @Test
    fun `given none return correct values`() {
        val setting = CosyncSetting.NONE
        assertThat(setting.canCoSyncAllData()).isFalse()
        assertThat(setting.canCoSyncAnalyticsData()).isFalse()
        assertThat(setting.canCoSyncBiometricData()).isFalse()
    }

    @Test
    fun `given biometrics return correct values`() {
        val setting = CosyncSetting.ONLY_BIOMETRICS
        assertThat(setting.canCoSyncAllData()).isFalse()
        assertThat(setting.canCoSyncAnalyticsData()).isFalse()
        assertThat(setting.canCoSyncBiometricData()).isTrue()
    }

    @Test
    fun `given analytics return correct values`() {
        val setting = CosyncSetting.ONLY_ANALYTICS
        assertThat(setting.canCoSyncAllData()).isFalse()
        assertThat(setting.canCoSyncAnalyticsData()).isTrue()
        assertThat(setting.canCoSyncBiometricData()).isFalse()
    }

    @Test
    fun `given all return correct values`() {
        val setting = CosyncSetting.ALL
        assertThat(setting.canCoSyncAllData()).isTrue()
        assertThat(setting.canCoSyncAnalyticsData()).isFalse()
        assertThat(setting.canCoSyncBiometricData()).isFalse()
    }
}
