package com.simprints.id.tools.serializers

import com.google.gson.Gson
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever
import junit.framework.Assert
import org.junit.Test

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class MapSerializerTest {

    companion object {
        private val originalInt = 1
        private val serializedInt = "1"
        private val originalBoolean = true
        private val serializedBoolean = "true"
        private val originalMap = mapOf(originalInt to originalBoolean)
    }

    private val intSerializer: Serializer<Int> = mockIntSerializer()
    private val booleanSerializer: Serializer<Boolean> = mockBooleanSerializer()
    private val mapSerializer = MapSerializer(intSerializer, booleanSerializer, Gson())

    private fun mockIntSerializer(): Serializer<Int> {
        val serializer = mock<Serializer<Int>>()
        whenever(serializer.serialize(originalInt)).thenReturn(serializedInt)
        whenever(serializer.deserialize(serializedInt)).thenReturn(originalInt)
        return serializer
    }

    private fun mockBooleanSerializer(): Serializer<Boolean> {
        val serializer = mock<Serializer<Boolean>>()
        whenever(serializer.serialize(originalBoolean)).thenReturn(serializedBoolean)
        whenever(serializer.deserialize(serializedBoolean)).thenReturn(originalBoolean)
        return serializer
    }


    @Test
    fun testSerializeThenDeserializeGivesOriginalEnumValue() {
        val serializedMap = mapSerializer.serialize(originalMap)
        val deserializedMap = mapSerializer.deserialize(serializedMap)
        Assert.assertEquals(originalMap, deserializedMap)
    }


}