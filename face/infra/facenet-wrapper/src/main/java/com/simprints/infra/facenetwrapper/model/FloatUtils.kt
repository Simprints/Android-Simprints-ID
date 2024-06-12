package com.simprints.infra.mlkitwrapper.tools

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

fun cosineSimilarity(x1: FloatArray, x2: FloatArray): Float {
    val mag1 = sqrt(x1.map { it * it }.sum())
    val mag2 = sqrt(x2.map { it * it }.sum())
    val dot = x1.mapIndexed { i, xi -> xi * x2[i] }.sum()
    return dot / (mag1 * mag2)
}

fun FloatArray.toBytes(): ByteArray {
    val bytes = ByteArray(size * Float.SIZE_BYTES)
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
    buffer.asFloatBuffer().put(this)
    return bytes
}

fun ByteArray.toFloats(): FloatArray {
    val floats = FloatArray(size / Float.SIZE_BYTES)
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)
    buffer.asFloatBuffer().get(floats)
    return floats
}

