package com.simprints.fingerprintscanner.v2.tools.primitives

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun byteArrayOf(vararg elements: Any): ByteArray {
    val bytes = mutableListOf<Byte>()
    elements.forEach {
        when (it) {
            is Byte -> bytes.add(it)
            is Int -> bytes.add(it.toByte())
            else -> throw IllegalArgumentException("Must provide either Byte or Int for byteArrayOf literal")
        }
    }
    return bytes.toByteArray()
}

fun concatenateByteArrays(vararg byteArrays: ByteArray): ByteArray =
    concatenateByteArrays(byteArrays.toList())

fun concatenateByteArrays(byteArrays: Iterable<ByteArray>): ByteArray =
    byteArrays.reduce { acc: ByteArray, bytes: ByteArray -> acc + bytes }

/**
 * @throws IndexOutOfBoundsException if position includes a range outside of the buffer
 * @throws java.nio.BufferUnderflowException if position includes a range that is too short
 */
fun <T> ByteArray.extract(getType: ByteBuffer.() -> T, position: IntRange? = null, byteOrder: ByteOrder): T =
    ByteBuffer.wrap(
        if (position != null)
            this.sliceArray(position)
        else
            this
    ).apply { order(byteOrder) }.getType()
