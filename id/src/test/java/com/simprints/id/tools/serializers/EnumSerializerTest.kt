package com.simprints.id.tools.serializers

import com.simprints.id.data.db.subject.domain.FingerIdentifier
import org.junit.Assert
import org.junit.Test

class EnumSerializerTest {

    private val enumClass = FingerIdentifier::class.java
    private val enumSerializer = EnumSerializer(enumClass)

    @Test
    fun testSerializeThenDeserializeGivesOriginalEnumValue() {
        enumClass.enumConstants?.let {
            for (originalEnumValue in it) {
                val serializedEnumValue = enumSerializer.serialize(originalEnumValue)
                val deserializedEnumValue = enumSerializer.deserialize(serializedEnumValue)
                Assert.assertEquals(originalEnumValue, deserializedEnumValue)
            }
        }

    }
}
