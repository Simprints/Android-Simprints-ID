package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.SyncDestinationSetting
import org.junit.Test

class SyncDestinationListSerializerTest {
    private val syncDestinationListSerializer = SyncDestinationListSerializer()

    @Test
    fun `serializing a list - one item`() {
        val destinationList = listOf(SyncDestinationSetting.COMMCARE)
        val expectedString = "COMMCARE"

        val serializedDestinations = syncDestinationListSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing a list - two items`() {
        val destinationList = listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.SIMPRINTS)
        val expectedString = "COMMCARE,SIMPRINTS"

        val serializedDestinations = syncDestinationListSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `deserializing a list - one item`() {
        val destinationString = "COMMCARE"
        val expectedList = listOf(SyncDestinationSetting.COMMCARE)

        val deserializedDestinations = syncDestinationListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - two items`() {
        val destinationString = "COMMCARE,SIMPRINTS"
        val expectedList = listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.SIMPRINTS)

        val deserializedDestinations = syncDestinationListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - bad format`() {
        val destinationString = """     COMMCARE   ,
                        SIMPRINTS
            """
        val expectedList = listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.SIMPRINTS)

        val deserializedDestinations = syncDestinationListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - inexistent option`() {
        val destinationString = "COMMCARE, DHIS2, SIMPRINTS 2, SIMPRINTS"
        val expectedList = listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.SIMPRINTS)

        val deserializedDestinations = syncDestinationListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - empty`() {
        val destinationString = ""
        val expectedList = listOf<SyncDestinationSetting>()

        val deserializedDestinations = syncDestinationListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

}
