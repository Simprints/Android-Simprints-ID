package com.simprints.fingerprintscanner.v2.tools.primitives

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.testtools.assertHexStringsEqual
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteOrder

class PrimitiveConversionToolsTest {

    @Test
    fun byteUnsignedToInt_worksForWholeRange() {
        assertEquals(10, 10.toByte().unsignedToInt())
        assertEquals(0, 0.toByte().unsignedToInt())
        assertEquals(250, 250.toByte().unsignedToInt())
        assertEquals(252, (-4).toByte().unsignedToInt())
        assertEquals(127, 127.toByte().unsignedToInt())
        assertEquals(128, 128.toByte().unsignedToInt())
        assertEquals(129, 129.toByte().unsignedToInt())
        assertEquals(255, (-1).toByte().unsignedToInt())
    }

    @Test
    fun shortUnsignedToInt_worksForWholeRange() {
        assertEquals(10, 10.toShort().unsignedToInt())
        assertEquals(0, 0.toShort().unsignedToInt())
        assertEquals(65530, 65530.toShort().unsignedToInt())
        assertEquals(65532, (-4).toShort().unsignedToInt())
        assertEquals(32767, 32767.toShort().unsignedToInt())
        assertEquals(32768, 32768.toShort().unsignedToInt())
        assertEquals(32769, 32769.toShort().unsignedToInt())
        assertEquals(65535, (-1).toShort().unsignedToInt())
    }

    @Test
    fun shortToByteArray_littleEndian_worksForWholeRange() {
        val byteOrder = ByteOrder.LITTLE_ENDIAN
        assertHexStringsEqual("57 00", 87.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("00 00", 0.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("FF FF", 65535.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("FF 00", 255.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("00 01", 256.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("17 36", 13847.toShort().toByteArray(byteOrder).toHexString())
    }

    @Test
    fun intToByteArray_littleEndian_worksForWholeRange() {
        val byteOrder = ByteOrder.LITTLE_ENDIAN
        assertHexStringsEqual("57 00 00 00", 87.toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("00 00 00 00", 0.toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("FF FF FF 7F", (Int.MAX_VALUE).toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("00 00 00 80", (Int.MIN_VALUE).toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("BB 5A 69 36", 912874171.toByteArray(byteOrder).toHexString())
    }

    @Test
    fun shortToByteArray_bigEndian_worksForWholeRange() {
        val byteOrder = ByteOrder.BIG_ENDIAN
        assertHexStringsEqual("00 57", 87.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("00 00", 0.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("FF FF", 65535.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("00 FF", 255.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("01 00", 256.toShort().toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("36 17", 13847.toShort().toByteArray(byteOrder).toHexString())
    }

    @Test
    fun intToByteArray_bigEndian_worksForWholeRange() {
        val byteOrder = ByteOrder.BIG_ENDIAN
        assertHexStringsEqual("00 00 00 57", 87.toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("00 00 00 00", 0.toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("7F FF FF FF", (Int.MAX_VALUE).toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("80 00 00 00", (Int.MIN_VALUE).toByteArray(byteOrder).toHexString())
        assertHexStringsEqual("36 69 5A BB", 912874171.toByteArray(byteOrder).toHexString())
    }

    @Test
    fun hexStringToByteArrayTest_worksForConformingStrings() {
        assertThat("10 3F 56".hexToByteArray()).isEqualTo(byteArrayOf(0x10, 0x3F, 0x56))
        assertThat("105fA\n 6".hexToByteArray()).isEqualTo(byteArrayOf(0x10, 0x5F, 0xA6))
        assertThat("dead    BEEF".hexToByteArray()).isEqualTo(byteArrayOf(0xde, 0xad, 0xbe, 0xef))
    }

    @Test
    fun hexStringToByteArrayTest_invalidStrings_throwsException() {
        assertThrows<IllegalArgumentException> { "10 3F 56 7".hexToByteArray() }
        assertThrows<IllegalArgumentException> { "dead beE".hexToByteArray() }
        assertThrows<IllegalArgumentException> { "dead borf".hexToByteArray() }
        assertThrows<IllegalArgumentException> { "'#'[.';".hexToByteArray() }
    }

    @Test
    fun byteArrayToHexString_works() {
        assertEquals("DE AD BE EF ", byteArrayOf(0xde, 0xad, 0xbe, 0xef).toHexString())
    }
}
