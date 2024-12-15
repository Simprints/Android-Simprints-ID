package com.simprints.fingerprint.infra.scanner.v2.domain

import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.extract
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toByteArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Interface for the general ideal of a protocol, which holds the rules for assembling bytes and
 * headers for the various APIs as necessary.
 */
interface Protocol {
    /**
     * Whether this protocol uses little-endian or big-endian byte order
     */
    val byteOrder: ByteOrder

    /**
     * Interpret bytes in a certain position within a ByteArray as a number (byte, short, int, long)
     */
    fun <T> ByteArray.extract(
        getType: ByteBuffer.() -> T,
        position: IntRange? = null,
    ): T = extract(getType, position, byteOrder)

    fun Short.toByteArray() = toByteArray(byteOrder)

    fun Int.toByteArray() = toByteArray(byteOrder)
}
