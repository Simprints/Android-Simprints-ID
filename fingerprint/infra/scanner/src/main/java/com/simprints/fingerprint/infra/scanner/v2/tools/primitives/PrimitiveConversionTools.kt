package com.simprints.fingerprint.infra.scanner.v2.tools.primitives

import java.nio.ByteOrder

/** Interprets negative bytes as if it where an unsigned byte (e.g. -0x01 -> 0xFF) */
fun Byte.unsignedToInt() = (this + Byte.MAX_VALUE - Byte.MIN_VALUE + 1) % (Byte.MAX_VALUE - Byte.MIN_VALUE + 1)

/** Interprets negative shorts as if it where an unsigned short (e.g. -0x0001 -> 0xFFFF) */
fun Short.unsignedToInt() = (this + Short.MAX_VALUE - Short.MIN_VALUE + 1) % (Short.MAX_VALUE - Short.MIN_VALUE + 1)

fun Short.toByteArray(byteOrder: ByteOrder): ByteArray = this.unsignedToInt().let {
    when (byteOrder) {
        ByteOrder.LITTLE_ENDIAN -> byteArrayOf(
            (it and 0xFFFF),
            ((it ushr 8) and 0xFFFF),
        )

        else -> byteArrayOf(
            ((it ushr 8) and 0xFFFF),
            (it and 0xFFFF),
        )
    }
}

fun Int.toByteArray(byteOrder: ByteOrder): ByteArray = let {
    when (byteOrder) {
        ByteOrder.LITTLE_ENDIAN -> byteArrayOf(
            (it and 0xFFFF),
            ((it ushr 8) and 0xFFFF),
            ((it ushr 16) and 0xFFFF),
            ((it ushr 24) and 0xFFFF),
        )

        else -> byteArrayOf(
            ((it ushr 24) and 0xFFFF),
            ((it ushr 16) and 0xFFFF),
            ((it ushr 8) and 0xFFFF),
            (it and 0xFFFF),
        )
    }
}

fun Long.toByteArray(byteOrder: ByteOrder): ByteArray = let {
    when (byteOrder) {
        ByteOrder.LITTLE_ENDIAN -> byteArrayOf(
            (it and 0xFFFF).toByte(),
            ((it ushr 8) and 0xFFFF).toByte(),
            ((it ushr 16) and 0xFFFF).toByte(),
            ((it ushr 24) and 0xFFFF).toByte(),
            ((it ushr 32) and 0xFFFF).toByte(),
            ((it ushr 40) and 0xFFFF).toByte(),
            ((it ushr 48) and 0xFFFF).toByte(),
            ((it ushr 56) and 0xFFFF).toByte(),
        )

        else -> byteArrayOf(
            ((it ushr 56) and 0xFFFF).toByte(),
            ((it ushr 48) and 0xFFFF).toByte(),
            ((it ushr 40) and 0xFFFF).toByte(),
            ((it ushr 32) and 0xFFFF).toByte(),
            ((it ushr 24) and 0xFFFF).toByte(),
            ((it ushr 16) and 0xFFFF).toByte(),
            ((it ushr 8) and 0xFFFF).toByte(),
            (it and 0xFFFF).toByte(),
        )
    }
}

/** @throws IllegalArgumentException if odd number of hex characters */
fun String.hexToByteArray(): ByteArray {
    val s = stripWhiteSpaceToLowercase()
    if (s.length % 2 != 0) throw IllegalArgumentException("String must contain even number of bytes")
    if ("""([^a-f\d])""".toRegex().containsMatchIn(
            s,
        )
    ) {
        throw IllegalArgumentException("String must contain only whitespace and hex characters (0-9,a-f,A-F)")
    }
    val data = ByteArray(s.length / 2)
    var i = 0

    while (i < s.length) {
        data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        i += 2
    }

    return data
}

fun ByteArray.toHexString() = StringBuilder()
    .apply {
        this@toHexString.forEach {
            var str = Integer.toHexString((it.toInt() + 256) % 256)
            str = when (str.length) {
                0 -> "00"
                1 -> "0$str"
                else -> str
            }.uppercase() + " "
            append(str)
        }
    }.toString()
