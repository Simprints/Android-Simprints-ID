package com.simprints.infra.security.random

import java.security.SecureRandom
import javax.inject.Inject

internal class RandomGeneratorImpl @Inject constructor() : RandomGenerator {
    override fun generateByteArray(length: Int): ByteArray {
        val key = ByteArray(length)
        SecureRandom().nextBytes(key)
        return key
    }
}
