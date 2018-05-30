package com.simprints.id.tools

import java.security.SecureRandom

class RandomGeneratorImpl : RandomGenerator {

    override fun generateByteArray(length: Int): ByteArray {
        val key = ByteArray(length)
        SecureRandom().nextBytes(key)
        return key
    }
}
