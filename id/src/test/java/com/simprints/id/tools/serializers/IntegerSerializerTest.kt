package com.simprints.id.tools.serializers

import org.junit.Test
import kotlin.test.assertEquals

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
