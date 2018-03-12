package com.simprints.id.secure.cryptography

import java.math.BigInteger
import java.security.MessageDigest


class Hasher {

    fun hash(string: String): String {
        val m = MessageDigest.getInstance("MD5")
        m.update(string.toByteArray(), 0, string.length)
        val i = BigInteger(1, m.digest())
        return String.format("%1$032x", i)
    }
}
