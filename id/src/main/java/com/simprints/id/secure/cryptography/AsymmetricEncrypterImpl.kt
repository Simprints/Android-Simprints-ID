package com.simprints.id.secure.cryptography

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher


open class AsymmetricEncrypterImpl(private val cipher: Cipher): AsymmetricEncrypter {

    override fun encrypt(string: String, publicKey: Key): String {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val plainBytes = string.toByteArray()
        val encryptedBytes = cipher.doFinal(plainBytes)
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    override fun decrypt(data: String, privateKey: Key): String {
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedData = Base64.decode(data, Base64.DEFAULT)
        val decodedData = cipher.doFinal(encryptedData)
        return String(decodedData)
    }
}
