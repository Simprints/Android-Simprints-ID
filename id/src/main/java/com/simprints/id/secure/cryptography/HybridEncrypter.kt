package com.simprints.id.secure.cryptography

interface HybridEncrypter {
    fun encrypt(plainString: String): String
    fun decrypt(encryptedString: String): String
}
