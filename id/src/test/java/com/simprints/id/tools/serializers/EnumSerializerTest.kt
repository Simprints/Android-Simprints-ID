package com.simprints.id.tools.serializers

import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Assert
import org.junit.Test
import java.security.InvalidParameterException

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

    @Test
    fun `throws an error when deserializing wrong value`() {
        assertThrows<InvalidParameterException> { enumSerializer.deserialize("UNKNOWN") }
    }

}
