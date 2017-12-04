package com.simprints.id.tools.serializers

import junit.framework.Assert.assertEquals
import org.junit.Test

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class BooleanSerializerTest {

    private val booleanSerializer = BooleanSerializer()

    @Test
    fun testSerializeThenDeserializeGivesOriginalBoolean() {
        for (originalBoolean in arrayOf(true, false)) {
            val serializedBoolean =  booleanSerializer.serialize(originalBoolean)
            val deserializedBoolean = booleanSerializer.deserialize(serializedBoolean)
            assertEquals(originalBoolean, deserializedBoolean)
        }
    }

}