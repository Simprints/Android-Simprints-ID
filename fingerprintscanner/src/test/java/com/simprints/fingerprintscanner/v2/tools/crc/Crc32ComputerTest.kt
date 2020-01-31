package com.simprints.fingerprintscanner.v2.tools.crc

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf
import org.junit.Test

class Crc32ComputerTest {

    private val crc32Computer = Crc32Computer()

    @Test
    fun computeCrc_withCypressCrcTable_computesCorrectly() {
        assertThat(crc32Computer.computeCrc32(byteArrayOf(0xFF, 0xFF, 0xFF, 0xFF)))
            .isEqualTo(0xFFFFFFFF.toInt())

        assertThat(crc32Computer.computeCrc32(byteArrayOf(0x01, 0x23, 0x45, 0x67, 0x89, 0xAB, 0xCD)))
            .isEqualTo(0xCA0340F1.toInt())
    }
}
