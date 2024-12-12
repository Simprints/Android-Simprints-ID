package com.simprints.fingerprint.infra.scanner.v2.tools.primitives

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.inv
import kotlin.experimental.xor

fun byteArrayOf(vararg elements: Any): ByteArray {
    val bytes = mutableListOf<Byte>()
    elements.forEach {
        when (it) {
            is Byte -> bytes.add(it)
            is Int -> bytes.add(it.toByte())
            is ByteArray -> bytes.addAll(it.toList())
            else -> throw IllegalArgumentException("Must provide either Byte or Int for byteArrayOf literal")
        }
    }
    return bytes.toByteArray()
}

/**
 * @throws IndexOutOfBoundsException if position includes a range outside of the buffer
 * @throws java.nio.BufferUnderflowException if position includes a range that is too short
 */
fun <T> ByteArray.extract(
    getType: ByteBuffer.() -> T,
    position: IntRange? = null,
    byteOrder: ByteOrder,
): T = ByteBuffer
    .wrap(
        if (position != null) {
            this.sliceArray(position)
        } else {
            this
        },
    ).apply { order(byteOrder) }
    .getType()

fun ByteArray.chunked(size: Int) = toList().chunked(size).map { it.toByteArray() }

fun ByteArray.xorAll() = reduce { acc, byte -> acc xor byte }

fun ByteArray.nxorAll() = xorAll().inv()

fun List<ByteArray>.pairWithProgress(): List<Pair<ByteArray, Float>> = mapIndexed { index, chunk ->
    Pair(chunk, (index + 1).toFloat() / this.size.toFloat())
}
