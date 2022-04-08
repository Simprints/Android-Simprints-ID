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
        defValue = SimprintsSyncSetting.NONE,
        serializer = simprintsSyncSettingEnumSerializer
    )

    private val coSyncSetting by RemoteConfigComplexPreference(
        prefs = prefs,
        remoteConfigWrapper = mockk(),
        key = coSyncSettingClass.name,
        defValue = CosyncSetting.NONE,
        serializer = coSyncSettingEnumSerializer
    )

    @Test
    fun `return correct value of all for simprints`() {
        every { prefs.getPrimitive(simprintsSyncSettingClass.name, DEFAULT_RETURN) } returns ALL
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.ALL)
    }

    @Test
    fun `return correct value of only analytics for simprints`() {
        every {
            prefs.getPrimitive(
                simprintsSyncSettingClass.name,
                DEFAULT_RETURN
            )
        } returns ONLY_ANALYTICS
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.ONLY_ANALYTICS)
    }

    @Test
    fun `return correct value of only biometrics for simprints`() {
        every {
            prefs.getPrimitive(
                simprintsSyncSettingClass.name,
                DEFAULT_RETURN
            )
        } returns ONLY_BIOMETRICS
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.ONLY_BIOMETRICS)
    }

    @Test
    fun `return correct value of only none for simprints`() {
        every {
            prefs.getPrimitive(
                simprintsSyncSettingClass.name,
                DEFAULT_RETURN
            )
        } returns DEFAULT_RETURN
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.NONE)
    }

    @Test
    fun `return default value if deserialization fails for simprints`() {
        every { prefs.getPrimitive(simprintsSyncSettingClass.name, DEFAULT_RETURN) } returns UNKNOWN
        assertThat(simprintsSyncSetting).isEqualTo(SimprintsSyncSetting.NONE)
    }

    @Test
    fun `return correct value of all for cosync`() {
        every { prefs.getPrimitive(coSyncSettingClass.name, DEFAULT_RETURN) } returns ALL
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.ALL)
    }

    @Test
    fun `return correct value of only analytics for cosync`() {
        every {
            prefs.getPrimitive(
                coSyncSettingClass.name,
                DEFAULT_RETURN
            )
        } returns ONLY_ANALYTICS
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.ONLY_ANALYTICS)
    }

    @Test
    fun `return correct value of only biometrics for cosync`() {
        every {
            prefs.getPrimitive(
                coSyncSettingClass.name,
                DEFAULT_RETURN
            )
        } returns ONLY_BIOMETRICS
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.ONLY_BIOMETRICS)
    }

    @Test
    fun `return correct value of only none for cosync`() {
        every {
            prefs.getPrimitive(
                coSyncSettingClass.name,
                DEFAULT_RETURN
            )
        } returns DEFAULT_RETURN
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.NONE)
    }

    @Test
    fun `return default value if deserialization fails for cosync`() {
        every { prefs.getPrimitive(coSyncSettingClass.name, DEFAULT_RETURN) } returns UNKNOWN
        assertThat(coSyncSetting).isEqualTo(CosyncSetting.NONE)
    }

    companion object {
        private const val DEFAULT_RETURN = "NONE"
        private const val UNKNOWN = "UNKNOWN_VALUE"
        private const val ONLY_BIOMETRICS = "ONLY_BIOMETRICS"
        private const val ONLY_ANALYTICS = "ONLY_ANALYTICS"
        private const val ALL = "ALL"
    }
}
