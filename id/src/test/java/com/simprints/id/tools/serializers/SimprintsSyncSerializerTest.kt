package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.SimprintsSyncSetting
import org.junit.Test

class SimprintsSyncSerializerTest {

    private val simprintsSyncSerializer = SimprintsSyncSerializer()

    @Test
    fun `serializing a list - one item`() {
        val destinationList = listOf(SimprintsSyncSetting.ALL)
        val expectedString = "ALL"

        val serializedDestinations = simprintsSyncSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing a list - all items`() {
        val destinationList =
            listOf(SimprintsSyncSetting.ONLY_BIOMETRICS, SimprintsSyncSetting.ONLY_ANALYTICS)
        val expectedString = "ONLY_BIOMETRICS,ONLY_ANALYTICS"

        val serializedDestinations = simprintsSyncSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `deserializing a list - empty`() {
        val destinationString = ""
        val expectedList = SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_SETTINGS_DEFAULT

        val deserialization = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - all options`() {
        val destinationString = "ALL, ONLY_BIOMETRICS, ONLY_ANALYTICS, NONE"
        val expectedList = listOf(
            SimprintsSyncSetting.ALL,
            SimprintsSyncSetting.ONLY_BIOMETRICS,
            SimprintsSyncSetting.ONLY_ANALYTICS,
            SimprintsSyncSetting.NONE
        )

        val deserialization = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - bad format`() {
        val destinationString = """     ALL   ,
                        SOME_VALUE
            """
        val expectedList = listOf(SimprintsSyncSetting.ALL)

        val deserializedDestinations = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - inexisted options`() {
        val destinationString = "ALL, ONLY_BIOMETRIC, ONLY_ANALYTIC, NONE"
        val expectedList = listOf(
            SimprintsSyncSetting.ALL,
            SimprintsSyncSetting.NONE
        )

        val deserialization = simprintsSyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expectedList)
    }
}
