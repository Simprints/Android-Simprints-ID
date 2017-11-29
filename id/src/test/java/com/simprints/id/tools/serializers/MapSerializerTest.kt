package com.simprints.id.tools.serializers

import com.google.gson.Gson
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class MapSerializerTest {

    companion object {
        private val anInt = 1
        private val aSerializedInt = "1"
        private val aBoolean = true
        private val aSerializedBoolean = "true"
        private val aMap = mapOf(anInt to aBoolean)
    }

    private lateinit var intSerializer: Serializer<Int>
    private lateinit var booleanSerializer: Serializer<Boolean>

    @Suppress("UNCHECKED_CAST")
    @Before
    fun setUp() {
        intSerializer = Mockito.mock(Serializer::class.java) as Serializer<Int>
        Mockito.`when`(intSerializer.serialize(anInt)).thenReturn(aSerializedInt)
        Mockito.`when`(intSerializer.deserialize(aSerializedInt)).thenReturn(anInt)
        booleanSerializer = Mockito.mock(Serializer::class.java) as Serializer<Boolean>
        Mockito.`when`(booleanSerializer.serialize(aBoolean)).thenReturn(aSerializedBoolean)
        Mockito.`when`(booleanSerializer.deserialize(aSerializedBoolean)).thenReturn(aBoolean)
    }

    @Test
    fun testConsistentSerialization() {
        val serializer = MapSerializer(intSerializer, booleanSerializer, Gson())
        Assert.assertEquals(aMap, serializer.deserialize(serializer.serialize(aMap)))
    }


}