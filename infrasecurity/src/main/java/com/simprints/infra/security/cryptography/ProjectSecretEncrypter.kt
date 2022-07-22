package com.simprints.infra.security.cryptography

import android.util.Base64
import android.util.Base64.NO_WRAP
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class ProjectSecretEncrypter(
    publicKey: String,
    private val cipher: Cipher = Cipher.getInstance(RSA_TRANSFORMATION)
) {

    private val publicKey: PublicKey = getPublicKeyFromBase64String(publicKey)

    fun encrypt(string: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val plainBytes = string.toByteArray()
        val encryptedBytes = cipher.doFinal(plainBytes)
        return Base64.encodeToString(encryptedBytes, NO_WRAP)
    }

    private fun getPublicKeyFromBase64String(publicKeyString: String): PublicKey {
        val publicKeyByteArray = Base64.decode(publicKeyString, NO_WRAP)
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(publicKeyByteArray))
    }

    companion object {
        private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
        private const val RSA_ALGORITHM = "RSA"
    }
}
