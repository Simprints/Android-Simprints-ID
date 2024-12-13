package com.simprints.infra.security.random

internal interface RandomGenerator {
    fun generateByteArray(length: Int = 64): ByteArray
}
