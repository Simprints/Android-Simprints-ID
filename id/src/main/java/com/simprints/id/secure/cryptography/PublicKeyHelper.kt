package com.simprints.id.secure.cryptography

import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

class PublicKeyHelper() {

    fun getPublicKey(): PublicKey {
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(PUBLIC_KEY.toByteArray()))
    }

    companion object {

        private const val RSA_ALGORITHM = "RSA"

        private const val PUBLIC_KEY = "GET A REAL PUBLIC KEY" // TODO
    }
}
