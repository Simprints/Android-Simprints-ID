package com.simprints.id.secure.cryptography

import android.util.Base64
import android.util.Base64.NO_WRAP
import com.simprints.id.secure.models.PublicKeyString
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class ProjectSecretEncrypter(publicKeyString: PublicKeyString,
                             rsaCipher: Cipher = Cipher.getInstance(RSA_TRANSFORMATION)): AsymmetricEncrypterImpl(rsaCipher) {

    private val publicKey: PublicKey = getPublicKeyFromBase64String(publicKeyString.value)

    fun encrypt(string: String): String = super.encrypt(string, publicKey)

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
