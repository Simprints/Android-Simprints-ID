package com.simprints.id.secure.cryptography

interface HybridCipher {
    fun encrypt(plainString: String): String
    fun decrypt(encryptedString: String): String
}
