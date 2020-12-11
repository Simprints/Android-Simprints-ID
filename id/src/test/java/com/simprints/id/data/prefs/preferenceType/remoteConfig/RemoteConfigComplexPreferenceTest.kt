package com.simprints.id.data.prefs.preferenceType.remoteConfig

import com.google.common.truth.Truth
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.tools.serializers.EnumSerializer
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class RemoteConfigComplexPreferenceTest {
    private val enumClass = SyncDestinationSetting::class.java
    private val enumSerializer = EnumSerializer(enumClass)
    val prefs = mockk<ImprovedSharedPreferences>()
    private val syncLocationSetting by RemoteConfigComplexPreference(
        prefs,
        mockk(),
        enumClass.name,
        SyncDestinationSetting.SIMPRINTS,
        enumSerializer
    )

    @Test
    fun `return correct value - equals to default`() {
        every { prefs.getPrimitive(enumClass.name, "SIMPRINTS") } returns "SIMPRINTS"
        Truth.assertThat(syncLocationSetting).isEqualTo(SyncDestinationSetting.SIMPRINTS)
    }

    @Test
    fun `return correct value - different from default`() {
        every { prefs.getPrimitive(enumClass.name, "SIMPRINTS") } returns "COMMCARE"
        Truth.assertThat(syncLocationSetting).isEqualTo(SyncDestinationSetting.COMMCARE)
    }

    @Test
    fun `return default value if deserialization fails`() {
        every { prefs.getPrimitive(enumClass.name, "SIMPRINTS") } returns "UNKNOWN"
        Truth.assertThat(syncLocationSetting).isEqualTo(SyncDestinationSetting.SIMPRINTS)
    }
}
