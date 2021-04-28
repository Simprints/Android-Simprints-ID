package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.domain.modality.Modality
import org.junit.Test

class ModalityListSerializerTest {

    private val modalitiesListSerializer = ModalitiesListSerializer()

    @Test
    fun `serializing a list - one item`() {
        val destinationList = listOf(Modality.FINGER)
        val expectedString = "FINGER"

        val serializedDestinations = modalitiesListSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `serializing a list - two items`() {
        val destinationList = listOf(Modality.FINGER, Modality.FACE)
        val expectedString = "FINGER,FACE"

        val serializedDestinations = modalitiesListSerializer.serialize(destinationList)

        assertThat(serializedDestinations).isEqualTo(expectedString)
    }

    @Test
    fun `deserializing a list - one item`() {
        val destinationString = "FINGER"
        val expectedList = listOf(Modality.FINGER)

        val deserializedDestinations = modalitiesListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - two items`() {
        val destinationString = "FINGER,FACE"
        val expectedList = listOf(Modality.FINGER, Modality.FACE)

        val deserializedDestinations = modalitiesListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - bad format`() {
        val destinationString = """     FINGER   ,
                        FACE
            """
        val expectedList = listOf(Modality.FINGER, Modality.FACE)

        val deserializedDestinations = modalitiesListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - inexistent option`() {
        val destinationString = "FINGER, PALM, FACE 2, FACE"
        val expectedList = listOf(Modality.FINGER, Modality.FACE)

        val deserializedDestinations = modalitiesListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }

    @Test
    fun `deserializing a list - empty`() {
        val destinationString = ""
        val expectedList = listOf<SyncDestinationSetting>()

        val deserializedDestinations = modalitiesListSerializer.deserialize(destinationString)
        assertThat(deserializedDestinations).isEqualTo(expectedList)
    }
}
