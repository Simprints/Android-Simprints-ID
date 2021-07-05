package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.SyncDestinationSetting
import org.junit.Test

class SyncDestinationListSerializerTest {
    private val syncDestinationListSerializer = SyncDestinationListSerializer()

    @Test
    fun `serializing a list - commcare item`() {
        val destinationList = listOf(SyncDestinationSetting.COMMCARE)
        val expectedString = "COMMCARE"

        val serializedDestinations = syncDestinationListSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing a list - cosync item`() {
        val destinationList = listOf(SyncDestinationSetting.COSYNC)
        val expectedString = "COSYNC"

        val serializedDestinations = syncDestinationListSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing a list - three items`() {
        val destinationList = listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.SIMPRINTS, SyncDestinationSetting.COSYNC)
        val expectedString = "COMMCARE,SIMPRINTS,COSYNC"

        val serializedDestinations = syncDestinationListSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `deserializing a list - commcare item`() {
        val destinationString = "COMMCARE"
        val expectedList = listOf(SyncDestinationSetting.COMMCARE)

        val deserializedDestinations = syncDestinationListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - cosync item`() {
        val destinationString = "COSYNC"
        val expectedList = listOf(SyncDestinationSetting.COSYNC)

        val deserializedDestinations = syncDestinationListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - three items`() {
        val destinationString = "COMMCARE,SIMPRINTS, COSYNC"
        val expectedList = listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.SIMPRINTS, SyncDestinationSetting.COSYNC)

        val deserializedDestinations = syncDestinationListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - bad format`() {
        val destinationString = """     COMMCARE   ,
                        SIMPRINTS,
                        COSYNC
            """
        val expectedList = listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.SIMPRINTS, SyncDestinationSetting.COSYNC)

        val deserializedDestinations = syncDestinationListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - inexistent option`() {
        val destinationString = "COMMCARE, DHIS2, COSYNC, SIMPRINTS 2, SIMPRINTS"
        val expectedList = listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.COSYNC, SyncDestinationSetting.SIMPRINTS)

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
