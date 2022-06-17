package com.simprints.id.data.prefs.preferenceType.remoteConfig

import com.google.common.truth.Truth.assertThat
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.id.domain.CosyncSetting
import com.simprints.id.domain.SimprintsSyncSetting
import com.simprints.id.tools.serializers.EnumSerializer
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class RemoteConfigComplexPreferenceTest {
    private val simprintsSyncSettingClass = SimprintsSyncSetting::class.java
    private val coSyncSettingClass = CosyncSetting::class.java
    private val simprintsSyncSettingEnumSerializer = EnumSerializer(simprintsSyncSettingClass)
    private val coSyncSettingEnumSerializer = EnumSerializer(coSyncSettingClass)
    val prefs = mockk<ImprovedSharedPreferences>()
    private val simprintsSyncSetting by RemoteConfigComplexPreference(
        prefs = prefs,
        remoteConfigWrapper = mockk(),
        key = simprintsSyncSettingClass.name,
        defValue = SimprintsSyncSetting.SIM_SYNC_NONE,
        serializer = simprintsSyncSettingEnumSerializer
    )

    private val coSyncSetting by RemoteConfigComplexPreference(
        prefs = prefs,
        remoteConfigWrapper = mockk(),
        key = coSyncSettingClass.name,
        defValue = CosyncSetting.COSYNC_NONE,
        serializer = coSyncSettingEnumSerializer
    )

    @Test
    fun `return correct value of all for simprints`() {
        every {
            prefs.getPrimitive(
                simprintsSyncSettingClass.name,
                SIM_SYNC_DEFAULT_RETURN
            )
        } returns SIM_SYNC_ALL
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_ALL)
    }

    @Test
    fun `return correct value of only analytics for simprints`() {
        every {
            prefs.getPrimitive(
                simprintsSyncSettingClass.name,
                SIM_SYNC_DEFAULT_RETURN
            )
        } returns SIM_SYNC_ONLY_ANALYTICS
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_ONLY_ANALYTICS)
    }

    @Test
    fun `return correct value of only biometrics for simprints`() {
        every {
            prefs.getPrimitive(
                simprintsSyncSettingClass.name,
                SIM_SYNC_DEFAULT_RETURN
            )
        } returns SIM_SYNC_ONLY_BIOMETRICS
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_ONLY_BIOMETRICS)
    }

    @Test
    fun `return correct value of only none for simprints`() {
        every {
            prefs.getPrimitive(
                simprintsSyncSettingClass.name,
                SIM_SYNC_DEFAULT_RETURN
            )
        } returns SIM_SYNC_DEFAULT_RETURN
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_NONE)
    }

    @Test
    fun `return default value if deserialization fails for simprints`() {
        every {
            prefs.getPrimitive(
                simprintsSyncSettingClass.name,
                SIM_SYNC_DEFAULT_RETURN
            )
        } returns UNKNOWN
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_NONE)
    }

    @Test
    fun `return correct value of all for cosync`() {
        every {
            prefs.getPrimitive(
                coSyncSettingClass.name,
                COSYNC_DEFAULT_RETURN
            )
        } returns COSYNC_ALL
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.COSYNC_ALL)
    }

    @Test
    fun `return correct value of only analytics for cosync`() {
        every {
            prefs.getPrimitive(
                coSyncSettingClass.name,
                COSYNC_DEFAULT_RETURN
            )
        } returns COSYNC_ONLY_ANALYTICS
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.COSYNC_ONLY_ANALYTICS)
    }

    @Test
    fun `return correct value of only biometrics for cosync`() {
        every {
            prefs.getPrimitive(
                coSyncSettingClass.name,
                COSYNC_DEFAULT_RETURN
            )
        } returns COSYNC_ONLY_BIOMETRICS
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.COSYNC_ONLY_BIOMETRICS)
    }

    @Test
    fun `return correct value of only none for cosync`() {
        every {
            prefs.getPrimitive(
                coSyncSettingClass.name,
                COSYNC_DEFAULT_RETURN
            )
        } returns COSYNC_DEFAULT_RETURN
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.COSYNC_NONE)
    }

    @Test
    fun `return default value if deserialization fails for cosync`() {
        every { prefs.getPrimitive(coSyncSettingClass.name, COSYNC_DEFAULT_RETURN) } returns UNKNOWN
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.COSYNC_NONE)
    }

    companion object {
        private const val SIM_SYNC_DEFAULT_RETURN = "SIM_SYNC_NONE"
        private const val SIM_SYNC_ALL = "SIM_SYNC_ALL"
        private const val SIM_SYNC_ONLY_BIOMETRICS = "SIM_SYNC_ONLY_BIOMETRICS"
        private const val SIM_SYNC_ONLY_ANALYTICS = "SIM_SYNC_ONLY_ANALYTICS"
        private const val COSYNC_DEFAULT_RETURN = "COSYNC_NONE"
        private const val COSYNC_ONLY_BIOMETRICS = "COSYNC_ONLY_BIOMETRICS"
        private const val COSYNC_ONLY_ANALYTICS = "COSYNC_ONLY_ANALYTICS"
        private const val COSYNC_ALL = "COSYNC_ALL"
        private const val UNKNOWN = "UNKNOWN_VALUE"
    }
}
