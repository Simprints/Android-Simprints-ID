package com.simprints.id.secure.cryptography

import com.google.common.io.BaseEncoding
import com.simprints.id.secure.models.PublicKeyString
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class AsymmetricEncrypter(publicKeyString: PublicKeyString) {

    private val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)
    private val publicKey: PublicKey = getPublicKeyFromBase64String(publicKeyString.value)

    fun encrypt(string: String): String {
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val plainBytes = string.toByteArray()
        val encryptedBytes = rsaCipher.doFinal(plainBytes)

        return BaseEncoding.base64().encode(encryptedBytes)
    }

    private fun getPublicKeyFromBase64String(publicKeyString: String): PublicKey {
        val publicKeyByteArray = BaseEncoding.base64().decode(publicKeyString)
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(publicKeyByteArray))
    }

    companion object {
        private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
        private const val RSA_ALGORITHM = "RSA"
    }
}
