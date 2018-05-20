package com.simprints.id.tools

interface RandomGenerator {

    fun generateByteArray(length: Int, seed: ByteArray): ByteArray
}
