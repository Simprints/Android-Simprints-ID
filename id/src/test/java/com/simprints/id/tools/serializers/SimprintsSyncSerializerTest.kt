package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.SimprintsSyncSetting
import org.junit.Test

class SimprintsSyncSerializerTest {

    private val simprintsSyncSerializer = SimprintsSyncSerializer()

    @Test
    fun `serializing ALL`() {
        val setting = SimprintsSyncSetting.SIM_SYNC_ALL
        val expectedString = "SIM_SYNC_ALL"

        val serializedDestinations = simprintsSyncSerializer.serialize(setting)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing BIOMETRICS`() {
        val setting = SimprintsSyncSetting.SIM_SYNC_ONLY_BIOMETRICS
        val expectedString = "SIM_SYNC_ONLY_BIOMETRICS"

        val serializedDestinations = simprintsSyncSerializer.serialize(setting)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `deserializing an empty string`() {
        val destinationString = ""
        val expected = SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_SETTINGS_DEFAULT

        val deserialization = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }

    @Test
    fun `deserializing ANALYTICS`() {
        val destinationString = "ONLY_ANALYTICS"
        val expected = SimprintsSyncSetting.SIM_SYNC_ONLY_ANALYTICS

        val deserialization = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }

    @Test
    fun `deserializing a string bad format ALL`() {
        val destinationString = """     ALL   
            """
        val expected = SimprintsSyncSetting.SIM_SYNC_ALL

        val deserialization = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }
}
