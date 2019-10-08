package com.simprints.fingerprintscanner.v2.tools.primitives

import java.nio.ByteOrder

fun Byte.unsignedToInt() = (this + Byte.MAX_VALUE - Byte.MIN_VALUE + 1) % (Byte.MAX_VALUE - Byte.MIN_VALUE + 1)

fun Short.unsignedToInt() = (this + Short.MAX_VALUE - Short.MIN_VALUE + 1) % (Short.MAX_VALUE - Short.MIN_VALUE + 1)

fun Short.toByteArray(byteOrder: ByteOrder): ByteArray =
    this.unsignedToInt().let {
        when (byteOrder) {
            ByteOrder.LITTLE_ENDIAN -> byteArrayOf(
                (it and 0xFFFF),
                ((it ushr 8) and 0xFFFF)
            )
            else -> byteArrayOf(
                ((it ushr 8) and 0xFFFF),
                (it and 0xFFFF)
            )
        }
    }

fun Int.toByteArray(byteOrder: ByteOrder): ByteArray =
    let {
        when (byteOrder) {
            ByteOrder.LITTLE_ENDIAN -> byteArrayOf(
                (it and 0xFFFF),
                ((it ushr 8) and 0xFFFF),
                ((it ushr 16) and 0xFFFF),
                ((it ushr 24) and 0xFFFF)
            )
            else -> byteArrayOf(
                ((it ushr 24) and 0xFFFF),
                ((it ushr 16) and 0xFFFF),
                ((it ushr 8) and 0xFFFF),
                (it and 0xFFFF)
            )
        }
    }

/** @throws IllegalArgumentException if odd number of hex characters */
fun String.hexToByteArray(): ByteArray {
    val s = stripWhiteSpaceAndMakeLowercase(this)
    if (s.length % 2 != 0) throw IllegalArgumentException("String must contain even number of bytes")
    if ("""([^a-f\d])""".toRegex().containsMatchIn(s)) throw IllegalArgumentException("String must contain only whitespace and hex characters (0-9,a-f,A-F)")
    val data = ByteArray(s.length / 2)
    var i = 0

    while (i < s.length) {
        data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        i += 2
    }

    return data
}

fun ByteArray.toHexString() = StringBuilder().apply {
    this@toHexString.forEach {
        var str = Integer.toHexString((it.toInt() + 256) % 256)
        str = when (str.length) {
            0 -> "00"
            1 -> "0$str"
            else -> str
        }.toUpperCase() + " "
        append(str)
    }
}.toString()
