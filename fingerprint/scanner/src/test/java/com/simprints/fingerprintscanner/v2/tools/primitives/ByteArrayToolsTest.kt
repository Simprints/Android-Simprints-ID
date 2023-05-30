package com.simprints.fingerprintscanner.v2.tools.primitives

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test
import java.nio.BufferUnderflowException
import java.nio.ByteOrder

class ByteArrayToolsTest {

    @Test
    fun byteArrayOfOverload_worksForValidArguments() {
        assertThat(byteArrayOf(0x10, 0x2A, 0x3F))
            .isEqualTo(kotlin.byteArrayOf(0x10, 0x2A, 0x3F))

        assertThat(byteArrayOf(0xFF, 0x7A, 0x80))
            .isEqualTo(kotlin.byteArrayOf(0xFF.toByte(), 0x7A, 0x80.toByte()))

        assertThat(byteArrayOf(255, 127, 0x10, -4, -0x12))
            .isEqualTo(kotlin.byteArrayOf(0xFF.toByte(), 0x7F, 0x10, 0xFC.toByte(), 0xEE.toByte()))

        assertThat(byteArrayOf(256, 512, 300))
            .isEqualTo(kotlin.byteArrayOf(0x00, 0x00, 0x2C))
    }

    @Test
    fun byteArrayOfOverload_throwsExceptionForInvalidArguments() {
        assertThrows<IllegalArgumentException> { byteArrayOf(0x10, 0x20, "dead beef") }
        assertThrows<IllegalArgumentException> { byteArrayOf(20, 20.toShort()) }
    }

    @Test
    fun extractValues_littleEndian_worksCorrectlyForValidArguments() {
        val bytes = byteArrayOf(0x12, 0x34, 0x56, 0x67, 0x89, 0xAB, 0xCD, 0xEF)
        val byteOrder = ByteOrder.LITTLE_ENDIAN

        assertThat(bytes.extract({ get() }, null, byteOrder))
            .isEqualTo(0x12)
        assertThat(bytes.extract({ get() }, 7..7, byteOrder))
            .isEqualTo(0xEF.toByte())

        assertThat(bytes.extract({ short }, null, byteOrder))
            .isEqualTo(13330)
        assertThat(bytes.extract({ short }, 4..5, byteOrder))
            .isEqualTo(43913 + 2 * Short.MIN_VALUE)

        assertThat(bytes.extract({ int }, null, byteOrder))
            .isEqualTo(1733702674)
        @Suppress("INTEGER_OVERFLOW")
        assertThat(bytes.extract({ int }, 4..7, byteOrder))
            .isEqualTo((4023233417 + 2 * Int.MIN_VALUE).toInt())
    }

    @Test
    fun extractValues_bigEndian_worksCorrectlyForValidArguments() {
        val bytes = byteArrayOf(0x12, 0x34, 0x56, 0x67, 0x89, 0xAB, 0xCD, 0xEF)
        val byteOrder = ByteOrder.BIG_ENDIAN

        assertThat(bytes.extract({ get() }, null, byteOrder))
            .isEqualTo(0x12)
        assertThat(bytes.extract({ get() }, 7..7, byteOrder))
            .isEqualTo(0xEF.toByte())

        assertThat(bytes.extract({ short }, null, byteOrder))
            .isEqualTo(4660)
        assertThat(bytes.extract({ short }, 4..5, byteOrder))
            .isEqualTo(35243 + 2 * Short.MIN_VALUE)

        assertThat(bytes.extract({ int }, null, byteOrder))
            .isEqualTo(305419879)
        @Suppress("INTEGER_OVERFLOW")
        assertThat(bytes.extract({ int }, 4..7, byteOrder))
            .isEqualTo((2309737967 + 2 * Int.MIN_VALUE).toInt())

        assertThat(bytes.extract({ long }, null, byteOrder))
            .isEqualTo(1311768394163015151)
        assertThat(bytes.extract({ long }, 0..7, byteOrder))
            .isEqualTo(1311768394163015151)
    }

    @Test
    fun extractValues_throwsCorrectExceptionForInvalidRange() {
        val bytes = byteArrayOf(0x12, 0x34, 0x56, 0x67, 0x89)
        val byteOrder = ByteOrder.LITTLE_ENDIAN

        assertThrows<IndexOutOfBoundsException> { bytes.extract({ int }, 2..5, byteOrder) }
        assertThrows<BufferUnderflowException> { bytes.extract({ int }, 0..2, byteOrder) }
    }

    @Test
    fun xorAll_evaluatesCorrectly() {
        assertThat(byteArrayOf(0x12, 0x34, 0x56, 0x67, 0x89).xorAll()).isEqualTo(0x9E.toByte())
        assertThat(byteArrayOf(0x12).xorAll()).isEqualTo(0x12.toByte())
    }

    @Test
    fun nxorAll_evaluatesCorrectly() {
        assertThat(byteArrayOf(0x12, 0x34, 0x56, 0x67, 0x89).nxorAll()).isEqualTo(0x61.toByte())
        assertThat(byteArrayOf(0x12).nxorAll()).isEqualTo(0xED.toByte())
    }
}
