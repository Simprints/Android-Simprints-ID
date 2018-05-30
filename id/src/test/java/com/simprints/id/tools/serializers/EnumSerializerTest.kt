package com.simprints.id.tools.serializers

import com.simprints.libsimprints.FingerIdentifier
import org.junit.Assert
import org.junit.Test

class EnumSerializerTest {

    private val enumClass = FingerIdentifier::class.java
    private val enumSerializer = EnumSerializer(enumClass)

    @Test
    fun testSerializeThenDeserializeGivesOriginalEnumValue() {
        for (originalEnumValue in enumClass.enumConstants) {
            val serializedEnumValue = enumSerializer.serialize(originalEnumValue)
            val deserializedEnumValue = enumSerializer.deserialize(serializedEnumValue)
            Assert.assertEquals(originalEnumValue, deserializedEnumValue)
        }
    }
}
