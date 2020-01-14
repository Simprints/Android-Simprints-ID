package com.simprints.id.data.secure.keystore

import com.simprints.id.exceptions.safe.callout.InvalidDecryptionData
import com.simprints.id.exceptions.unexpected.MissingPrivateKeyInKeystoreException
import com.simprints.id.secure.cryptography.AsymmetricEncrypter
import com.simprints.id.secure.cryptography.AsymmetricEncrypterImpl
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import javax.crypto.Cipher

open class KeystoreManagerImpl(private val asymmetricEncrypter: AsymmetricEncrypter = AsymmetricEncrypterImpl(Cipher.getInstance(TRANSFORMATION))) : KeystoreManager {

    companion object {
        private const val KEY_ALIAS = "masterKey"
        private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"
        private const val ANDROID_KEY_STORE_PROVIDER = "AndroidKeyStore"

        private const val SEED = "seed"
    }

    override fun decryptString(string: String): String {
        val keyPair = getKeyPair() ?: throw MissingPrivateKeyInKeystoreException()

        return asymmetricEncrypter.decrypt(string, keyPair.private).let {
            if (it.startsWith(SEED))
                it.removePrefix(SEED)
            else throw InvalidDecryptionData()
        }
    }

    private fun getKeyPair(): KeyPair? {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_PROVIDER).apply { load(null) }

        val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey?
        val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey

        return if (privateKey != null && publicKey != null) {
            KeyPair(publicKey, privateKey)
        } else {
            null
        }
    }
}
