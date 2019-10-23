package com.simprints.id.secure.cryptography

import android.content.Context
import com.facebook.android.crypto.keychain.AndroidConceal
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.CryptoConfig
import com.facebook.crypto.Entity

class HybridCipherImpl(private val context: Context) : HybridCipher {

    private val crypto by lazy {
        val keyChain = SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256)
        AndroidConceal.get().createDefaultCrypto(keyChain)
    }

    private val entity = Entity.create(ENTITY_NAME)

    override fun encrypt(plainString: String): String {
        val encryptedBytes = crypto.encrypt(plainString.toByteArray(), entity)
        return String(encryptedBytes, encryptionCharset)
    }

    override fun decrypt(encryptedString: String): String {
        val encryptedBytes = encryptedString.toByteArray(encryptionCharset)
        val decryptedBytes = crypto.decrypt(encryptedBytes, entity)
        return String(decryptedBytes)
    }

    companion object {
        private const val ENTITY_NAME = "simprints_entity"

        private val encryptionCharset = Charsets.ISO_8859_1
    }

}
