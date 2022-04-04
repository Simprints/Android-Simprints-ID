package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.CosyncSetting
import org.junit.Test

class CosyncSerializerTest {

    private val cosyncSerializer = CosyncSerializer()

    @Test
    fun `serializing a list - one item`() {
        val destinationList = listOf(CosyncSetting.ALL)
        val expectedString = "ALL"

        val serializedDestinations = cosyncSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing a list - all items`() {
        val destinationList =
            listOf(CosyncSetting.ONLY_BIOMETRICS, CosyncSetting.ONLY_ANALYTICS)
        val expectedString = "ONLY_BIOMETRICS,ONLY_ANALYTICS"

        val serializedDestinations = cosyncSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `deserializing a list - empty`() {
        val destinationString = ""
        val expectedList = SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_SETTINGS_DEFAULT

        val deserialization = cosyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - all options`() {
        val destinationString = "ALL, ONLY_BIOMETRICS, ONLY_ANALYTICS, NONE"
        val expectedList = listOf(
            CosyncSetting.ALL,
            CosyncSetting.ONLY_BIOMETRICS,
            CosyncSetting.ONLY_ANALYTICS,
            CosyncSetting.NONE
        )

        val deserialization = cosyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - bad format`() {
        val destinationString = """     ALL   ,
                        SOME_VALUE
            """
        val expectedList = listOf(CosyncSetting.ALL)

        val deserializedDestinations = cosyncSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - inexisted options`() {
        val destinationString = "ALL, ONLY_BIOMETRIC, ONLY_ANALYTIC, NONE"
        val expectedList = listOf(
            CosyncSetting.ALL,
            CosyncSetting.NONE
        )

        val deserialization = cosyncSerializer.deserialize(destinationString)
        assertThat(deserialization).isEqualTo(expectedList)
    }
}
