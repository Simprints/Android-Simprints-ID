package com.simprints.fingerprintscannermock

import com.simprints.fingerprintscanner.v1.Message
import com.simprints.fingerprintscannermock.simulated.tools.hexStringToByteArray
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer


class ByteArrayUtilsTest {

    @Test
    fun convertingStringToByteArrayAndBack_works() {
        val string = "F0 F0 F0 F0 F0 02 02 02 01 00 99 AA BB CC D3 3d ".toLowerCase()
        val bytes = hexStringToByteArray(string)
        val newString = Message.hexString(ByteBuffer.wrap(bytes), 0, bytes.size)
        assertEquals(string, newString)
    }
}
