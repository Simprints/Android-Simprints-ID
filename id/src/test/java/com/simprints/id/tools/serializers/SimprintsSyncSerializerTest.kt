package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.SimprintsSyncSetting
import org.junit.Test

class SimprintsSyncSerializerTest {

    private val simprintsSyncSerializer = SimprintsSyncSerializer()

    @Test
    fun `serializing a list - returns ALL`() {
        val dest = SimprintsSyncSetting.ALL
        val expectedString = "ALL"

        val serializedDestinations = simprintsSyncSerializer.serialize(dest)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing a list - returns ONLY BIOMETRICS`() {
        val dest = SimprintsSyncSetting.ONLY_BIOMETRICS
        val expectedString = "ONLY_BIOMETRICS"

        val serializedDestinations = simprintsSyncSerializer.serialize(dest)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `deserializing a list - empty returns DEFAULT`() {
        val destinationString = ""
        val expected = SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_SETTINGS_DEFAULT

        val deserialization = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }

    @Test
    fun `deserializing a list - returns ALL`() {
        val destinationString = "ALL"
        val expected = SimprintsSyncSetting.ALL

        val deserialization = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }

    @Test
    fun `deserializing a list - bad format returns NONE`() {
        val destinationString = """     NONE   
            """
        val expected = SimprintsSyncSetting.NONE

        val deserializedDestinations = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expected)
    }

    @Test
    fun `deserializing a list - inexisted options returns default`() {
        val destinationString = "ALAL, ONLYY_BIOMETRIC, ONLY_ANALYTIC, NONEE"
        val expected = SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_SETTINGS_DEFAULT

        val deserialization = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expected)
    }
}
