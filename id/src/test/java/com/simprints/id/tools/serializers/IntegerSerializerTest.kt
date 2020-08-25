package com.simprints.id.tools.serializers

import org.junit.Test
import org.junit.jupiter.api.Assertions.*

class IntegerSerializerTest {

    private val integerSerializer = IntegerSerializer()

    @Test
    fun testSerializeThenDeserializeGivesOriginalInteger() {
        val integer = 45
        val serializedInt = integerSerializer.serialize(integer)
        val deserializedInt = integerSerializer.deserialize(serializedInt)

        assertEquals(integer, deserializedInt)
    }
}
