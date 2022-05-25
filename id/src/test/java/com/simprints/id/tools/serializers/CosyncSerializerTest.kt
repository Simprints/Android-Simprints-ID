package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.CosyncSetting
import org.junit.Test

class CosyncSerializerTest {

    private val cosyncSerializer = CosyncSerializer()

    @Test
    fun `serializing ALL`() {
        val setting = CosyncSetting.COSYNC_ALL
        val expectedString = "COSYNC_ALL"

        val serializedDestinations = cosyncSerializer.serialize(setting)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing BIOMETRICS`() {
        val setting = CosyncSetting.COSYNC_ONLY_BIOMETRICS
        val expectedString = "COSYNC_ONLY_BIOMETRICS"

        val serializedDestinations = cosyncSerializer.serialize(setting)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `deserializing an empty string`() {
        val destinationString = ""
        val expected = SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_SETTINGS_DEFAULT

        val deserialization = cosyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }

    @Test
    fun `deserializing ANALYTICS`() {
        val destinationString = "ONLY_ANALYTICS"
        val expected = CosyncSetting.COSYNC_ONLY_ANALYTICS

        val deserialization = cosyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }

    @Test
    fun `deserializing a string bad format ALL`() {
        val destinationString = """     ALL   
            """
        val expected = CosyncSetting.COSYNC_ALL

        val deserialization = cosyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }
}
