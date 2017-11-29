package com.simprints.id.tools.serializers

import com.simprints.libsimprints.FingerIdentifier
import junit.framework.Assert
import org.junit.Test

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class FingerIdentifierSerializerTest {

    @Test
    fun testConsistentSerialization() {
        val serializer = FingerIdentifierSerializer()
        for (fingerId in FingerIdentifier.values())
        Assert.assertEquals(fingerId, serializer.deserialize(serializer.serialize(fingerId)))
    }

}