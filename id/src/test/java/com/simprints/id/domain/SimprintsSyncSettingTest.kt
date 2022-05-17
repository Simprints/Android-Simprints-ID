package com.simprints.id.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SimprintsSyncSettingTest {

    @Test
    fun `given none return correct values`() {
        val setting = SimprintsSyncSetting.NONE
        assertThat(setting.canSyncDataToSimprints()).isFalse()
        assertThat(setting.canSyncAnalyticsDataToSimprints()).isFalse()
        assertThat(setting.canSyncBiometricDataToSimprints()).isFalse()
    }

    @Test
    fun `given biometrics return correct values`() {
        val setting = SimprintsSyncSetting.ONLY_BIOMETRICS
        assertThat(setting.canSyncDataToSimprints()).isTrue()
        assertThat(setting.canSyncAnalyticsDataToSimprints()).isFalse()
        assertThat(setting.canSyncBiometricDataToSimprints()).isTrue()
    }

    @Test
    fun `given analytics return correct values`() {
        val setting = SimprintsSyncSetting.ONLY_ANALYTICS
        assertThat(setting.canSyncDataToSimprints()).isTrue()
        assertThat(setting.canSyncAnalyticsDataToSimprints()).isTrue()
        assertThat(setting.canSyncBiometricDataToSimprints()).isFalse()
    }

    @Test
    fun `given all return correct values`() {
        val setting = SimprintsSyncSetting.ALL
        assertThat(setting.canSyncDataToSimprints()).isTrue()
        assertThat(setting.canSyncAnalyticsDataToSimprints()).isFalse()
        assertThat(setting.canSyncBiometricDataToSimprints()).isFalse()
    }
}
