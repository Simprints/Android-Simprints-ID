package com.simprints.id.tools

import java.security.SecureRandom

class RandomGeneratorImpl : RandomGenerator {

    override fun generateByteArray(length: Int, seed: ByteArray): ByteArray {
        val key = ByteArray(length)
        SecureRandom(seed).nextBytes(key)
        return key
    }
}
