package com.simprints.id.secure.cryptography

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class AsymmetricEncryptionHelper {

    fun getPublicKeyFromBase64String(publicKeyString: String): PublicKey {
        val publicKeyByteArray = Base64.decode(publicKeyString, Base64.DEFAULT)
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(publicKeyByteArray))
    }

    fun getCipher(): Cipher {
        return Cipher.getInstance(RSA_ALGORITHM)
    }

    companion object {

        private const val RSA_ALGORITHM = "RSA"
    }
}
