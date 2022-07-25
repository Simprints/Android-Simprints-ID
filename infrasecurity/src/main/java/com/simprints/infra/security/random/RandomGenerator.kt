package com.simprints.infra.security.random

interface RandomGenerator {

    fun generateByteArray(length: Int = 64): ByteArray
}
