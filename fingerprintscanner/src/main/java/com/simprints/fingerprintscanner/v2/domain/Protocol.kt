package com.simprints.fingerprintscanner.v2.domain

import com.simprints.fingerprintscanner.v2.tools.primitives.extract
import com.simprints.fingerprintscanner.v2.tools.primitives.toByteArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface Protocol {

    val byteOrder: ByteOrder

    fun <T> ByteArray.extract(getType: ByteBuffer.() -> T, position: IntRange? = null): T =
        extract(getType, position, byteOrder)

    fun Short.toByteArray() =
        toByteArray(byteOrder)

    fun Int.toByteArray() =
        toByteArray(byteOrder)
}
