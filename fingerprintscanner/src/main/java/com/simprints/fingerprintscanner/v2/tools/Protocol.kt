package com.simprints.fingerprintscanner.v2.tools

import java.nio.ByteBuffer
import java.nio.ByteOrder

interface Protocol {

    val byteOrder: ByteOrder

    fun <T> ByteArray.extract(getType: ByteBuffer.() -> T, position: IntRange? = null): T =
        extract(getType, position, byteOrder)
}
