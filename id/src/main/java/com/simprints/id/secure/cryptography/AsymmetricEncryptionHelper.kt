package com.simprints.id.secure.cryptography

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class AsymmetricEncryptionHelper {

    fun getPublicKey(): PublicKey {
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(PUBLIC_KEY))
    }

    fun getCipher(): Cipher {
        return Cipher.getInstance(RSA_ALGORITHM)
    }

    companion object {

        private const val RSA_ALGORITHM = "RSA"

        private val PUBLIC_KEY = Base64.decode("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMu9L/Apu2nWnBhcBAK" +
            "w+q23vHQ1KSupMgoIO+XZD5BTo3vNkXt2Jqs2xVIKJmRE1yM7Sz0BlOXDxyVasTHXuPaL9OJ0+BRXx3lXrK/" +
            "Y62LphM/aeHA3m4JacP8S3C5m4ZZieg2h61tzcB1UZFiinR4IpRDhpw85y109Tj4Ar4dwIDAQAB", Base64.DEFAULT) // TODO : Get a different one
    }
}
