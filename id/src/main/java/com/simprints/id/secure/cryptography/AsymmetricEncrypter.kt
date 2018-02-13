package com.simprints.id.secure.cryptography

import com.google.common.io.BaseEncoding
import com.simprints.id.secure.models.PublicKeyString
import java.security.PublicKey
import javax.crypto.Cipher

class AsymmetricEncrypter(publicKeyString: PublicKeyString) {

    private val rsaCipher: Cipher = AsymmetricEncryptionHelper().getCipher()
    private val publicKey: PublicKey = AsymmetricEncryptionHelper().getPublicKeyFromBase64String(publicKeyString.value)

    fun encrypt(string: String): String {
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val plainBytes = string.toByteArray()
        val encryptedBytes = rsaCipher.doFinal(plainBytes)

        return BaseEncoding.base64().encode(encryptedBytes)
    }
}
