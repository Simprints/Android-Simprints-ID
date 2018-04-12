package com.simprints.id.tools.serializers

import com.simprints.id.domain.Location
import org.junit.Assert
import org.junit.Test

class LocationSerializerTest {

    private val locationSerializer = LocationSerializer()

    @Test
    fun testSerializeThenDeserializeGivesOriginalLocation() {
        val originalLocation = Location("52.2170303", "0.1400018")
        val serializedLocation = locationSerializer.serialize(originalLocation)
        val deserializedLocation = locationSerializer.deserialize(serializedLocation)
        Assert.assertEquals(originalLocation, deserializedLocation)
    }
}
