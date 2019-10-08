package com.simprints.fingerprintscanner.v2.tools.primitives

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

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
}
