package com.simprints.id.secure.cryptography

import java.security.Key

interface AsymmetricEncrypter {

    fun encrypt(string: String, publicKey: Key): String
    fun decrypt(data: String, privateKey: Key): String
}
