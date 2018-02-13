package com.simprints.id.secure.cryptography

import com.google.common.io.BaseEncoding
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class AsymmetricEncryptionHelper {

    fun getPublicKeyFromBase64String(publicKeyString: String): PublicKey {
        val publicKeyByteArray = BaseEncoding.base64().decode(publicKeyString)
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(publicKeyByteArray))
    }

    fun getCipher(): Cipher {
        return Cipher.getInstance(RSA_TRANSFORMATION)
    }

    companion object {

        private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
        private const val RSA_ALGORITHM = "RSA"
    }
}
