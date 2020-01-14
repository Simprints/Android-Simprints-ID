package com.simprints.id.tools

interface RandomGenerator {

    fun generateByteArray(length: Int = 64): ByteArray
}
