package com.simprints.id.data.secure.keystore

import android.annotation.TargetApi
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.simprints.id.exceptions.safe.callout.InvalidDecryptionData
import com.simprints.id.exceptions.unexpected.MissingPrivateKeyInKeystoreError
import com.simprints.id.secure.cryptography.AsymmetricEncrypter
import com.simprints.id.secure.cryptography.AsymmetricEncrypterImpl
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

open class KeystoreManagerImpl(private val context: Context,
                               private val asymmetricEncrypter: AsymmetricEncrypter = AsymmetricEncrypterImpl(Cipher.getInstance(TRANSFORMATION))) : KeystoreManager {

    companion object {
        private const val RSA = "RSA"
        private const val KEY_ALIAS = "masterKey"
        private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"
        private const val ANDROID_KEY_STORE_PROVIDER = "AndroidKeyStore"

        private const val SEED = "seed"
    }

    override fun decryptString(string: String): String {
        val keyPair = getKeyPair() ?: throw MissingPrivateKeyInKeystoreError()

        return asymmetricEncrypter.decrypt(string, keyPair.private).let {
            if (it.startsWith(SEED))
                it.removePrefix(SEED)
            else throw InvalidDecryptionData()
        }
    }

    override fun encryptString(string: String): String {
        val keyPair: KeyPair = getKeyPair() ?: createAndSaveKeyPair()
        return asymmetricEncrypter.encrypt(SEED + string, keyPair.public)
    }

    private fun createAndSaveKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(RSA, ANDROID_KEY_STORE_PROVIDER)

        if (SDK_INT >= M) {
            initGeneratorWithKeyGenParameterSpec(generator)
        } else {
            initGeneratorWithKeyPairGeneratorSpec(generator)
        }

        // Generates Key with given spec and saves it to the KeyStore
        return generator.generateKeyPair()
    }

    private fun initGeneratorWithKeyPairGeneratorSpec(generator: KeyPairGenerator) {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 20)

        @Suppress("DEPRECATION")
        val builder = KeyPairGeneratorSpec.Builder(context)
            .setAlias(KEY_ALIAS)
            .setSerialNumber(BigInteger.ONE)
            .setSubject(X500Principal("CN=$KEY_ALIAS CA Certificate"))
            .setStartDate(startDate.time)
            .setEndDate(endDate.time)

        generator.initialize(builder.build())
    }

    @TargetApi(M)
    private fun initGeneratorWithKeyGenParameterSpec(generator: KeyPairGenerator) {
        val builder = KeyGenParameterSpec.Builder(KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        generator.initialize(builder.build())
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
