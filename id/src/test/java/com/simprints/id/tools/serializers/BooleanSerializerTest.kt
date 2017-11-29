package com.simprints.id.tools.serializers

import junit.framework.Assert
import org.junit.Test

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class BooleanSerializerTest {

    @Test
    fun testConsistentSerialization() {
        val serializer = BooleanSerializer()
        for (boolean in arrayOf(true, false))
            Assert.assertEquals(boolean, serializer.deserialize(serializer.serialize(boolean)))
    }

}