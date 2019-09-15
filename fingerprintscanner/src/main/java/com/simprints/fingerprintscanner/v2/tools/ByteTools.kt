package com.simprints.fingerprintscanner.v2.tools

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** @throws IllegalArgumentException if odd number of hex characters */
fun hexStringToByteArray(string: String): ByteArray {
    val s = stripWhiteSpaceAndMakeLowercase(string)
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

fun String.toByteArray() = hexStringToByteArray(this)

fun ByteArray.appendCheckSum(): ByteArray = this + byteArrayOf(calculateCheckSum(this))

fun calculateCheckSum(bytes: ByteArray): Byte = bytes.sum().toByte()

fun isCheckSumValid(bytes: ByteArray): Boolean {
    // The Checksum is the last byte, which is equal to the sum of all other bytes mod 256
    val expectedCheckSum = bytes.last()
    val actualCheckSum =
        calculateCheckSum(bytes.slice(0 until (bytes.size - 1)).toByteArray())
    return expectedCheckSum == actualCheckSum
}

fun stripWhiteSpaceAndMakeLowercase(string: String) = string
    .replace(" ", "")
    .replace("\n", "")
    .replace("\r", "")
    .toLowerCase()

fun concatenateByteArrays(vararg byteArrays: ByteArray): ByteArray =
    concatenateByteArrays(byteArrays.toList())

fun concatenateByteArrays(byteArrays: Iterable<ByteArray>): ByteArray =
    byteArrays.reduce { acc: ByteArray, bytes: ByteArray -> acc + bytes }

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

fun Byte.unsignedToInt() = (this + Byte.MAX_VALUE - Byte.MIN_VALUE + 1) % (Byte.MAX_VALUE - Byte.MIN_VALUE + 1)
fun Short.unsignedToInt() = (this + Short.MAX_VALUE - Short.MIN_VALUE + 1) % (Short.MAX_VALUE - Short.MIN_VALUE + 1)

fun Short.toByteArray(): ByteArray { // TODO : This assumes little-endian
    val value = this.unsignedToInt()
    val bytes = ByteArray(2)
    bytes[0] = (value and 0xFFFF).toByte()
    bytes[1] = ((value ushr 8) and 0xFFFF).toByte()
    return bytes
}

fun Int.to3ByteArray(): ByteArray {
    val value = this
    val bytes = ByteArray(3)
    bytes[0] = (value and 0xFFFF).toByte()
    bytes[1] = ((value ushr 8) and 0xFFFF).toByte()
    bytes[2] = ((value ushr 16) and 0xFFFF).toByte()
    return bytes
}

fun Int.toByteArray(): ByteArray {
    val value = this
    val bytes = ByteArray(4)
    bytes[0] = (value and 0xFFFF).toByte()
    bytes[1] = ((value ushr 8) and 0xFFFF).toByte()
    bytes[2] = ((value ushr 16) and 0xFFFF).toByte()
    bytes[3] = ((value ushr 24) and 0xFFFF).toByte()
    return bytes
}

/** @throws IndexOutOfBoundsException */
fun <T> ByteArray.extract(getType: ByteBuffer.() -> T, position: IntRange? = null, byteOrder: ByteOrder): T =
    ByteBuffer.wrap(
        if (position != null)
            this.sliceArray(position)
        else
            this
    ).apply { order(byteOrder) }.getType()
