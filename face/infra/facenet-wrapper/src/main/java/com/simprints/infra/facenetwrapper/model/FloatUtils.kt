package com.simprints.infra.mlkitwrapper.tools

import kotlin.math.sqrt

fun cosineSimilarity(x1: FloatArray, x2: FloatArray): Float {
    val mag1 = sqrt(x1.map { it * it }.sum())
    val mag2 = sqrt(x2.map { it * it }.sum())
    val dot = x1.mapIndexed { i, xi -> xi * x2[i] }.sum()
    return dot / (mag1 * mag2)
}
