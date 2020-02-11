package com.simprints.id.tools.serializers

import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class MapSerializerTest {

    companion object {
        private const val originalInt = 1
        private const val serializedInt = "1"
        private const val originalBoolean = true
        private const val serializedBoolean = "true"
        private val originalMap = mapOf(originalInt to originalBoolean)
    }

    private val intSerializer: Serializer<Int> = mockIntSerializer()
    private val booleanSerializer: Serializer<Boolean> = mockBooleanSerializer()
    private val mapSerializer = MapSerializer(intSerializer, booleanSerializer, Gson())

    private fun mockIntSerializer(): Serializer<Int> {
        val serializer = mockk<Serializer<Int>>()
        every { serializer.serialize(originalInt) } returns serializedInt
        every { serializer.deserialize(serializedInt) } returns originalInt
        return serializer
    }

    private fun mockBooleanSerializer(): Serializer<Boolean> {
        val serializer = mockk<Serializer<Boolean>>()
        every { serializer.serialize(originalBoolean) } returns serializedBoolean
        every { serializer.deserialize(serializedBoolean) } returns originalBoolean
        return serializer
    }

    @Test
    fun testSerializeThenDeserializeGivesOriginalEnumValue() {
        val serializedMap = mapSerializer.serialize(originalMap)
        val deserializedMap = mapSerializer.deserialize(serializedMap)
        Assert.assertEquals(originalMap, deserializedMap)
    }
}
