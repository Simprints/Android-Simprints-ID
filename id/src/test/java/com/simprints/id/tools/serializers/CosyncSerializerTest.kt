package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.CosyncSetting
import org.junit.Test

class CosyncSerializerTest {

    private val cosyncSerializer = CosyncSerializer()

    @Test
    fun `serializing a list - one item`() {
        val dest = CosyncSetting.ALL
        val expectedString = "ALL"

        val serializedDestinations = cosyncSerializer.serialize(dest)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing a list - all items`() {
        val dest = CosyncSetting.ONLY_ANALYTICS
        val expectedString = "ONLY_ANALYTICS"

        val serializedDestinations = cosyncSerializer.serialize(dest)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `deserializing a list - empty`() {
        val dest = ""
        val expectedList = SettingsPreferencesManagerImpl.COSYNC_SYNC_SETTINGS_DEFAULT

        val deserialization = cosyncSerializer.deserialize(dest)
        assertThat(deserialization).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - all options`() {
        val destinationString = "NONE"
        val expected = CosyncSetting.NONE

        val deserialization = cosyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }

    @Test
    fun `deserializing a list - bad format`() {
        val destinationString = """     ONLY_BIOMETRICS   
            """
        val expected = CosyncSetting.ONLY_BIOMETRICS

        val deserializedDestinations = cosyncSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expected)
    }

    @Test
    fun `deserializing a list - inexisted options`() {
        val destinationString = "ALL"
        val expected = CosyncSetting.ALL

        val deserialization = cosyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }
}
