package com.simprints.id.secure.cryptography

import android.util.Base64
import android.util.Base64.NO_WRAP
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
