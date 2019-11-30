package com.simprints.id.data.secure

import android.content.SharedPreferences
import android.util.Base64.*
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.RandomGeneratorImpl
import timber.log.Timber

open class SecureLocalDbKeyProviderImpl(private val encryptedSharedPrefs: SharedPreferences,
                                   private val randomGenerator: RandomGenerator = RandomGeneratorImpl(),
                                   private val unsecuredLocalDbKeyProvider: LegacyLocalDbKeyProvider): SecureLocalDbKeyProvider {

    companion object {
        const val REALM_KEY = "REALM_KEY"
    }

    override fun setLocalDatabaseKey(projectId: String) {
        var key = readRealmKeyFromSharedPrefs(projectId)
        if(key.isNullOrEmpty()) {
            key = generateRealmKey()
            encryptedSharedPrefs.edit().putString(getSharedPrefsKeyForRealm(projectId), key).apply()
        }
    }

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey {
        try {
            val key = readRealmKeyFromSharedPrefs(projectId)
                ?: throw MissingLocalDatabaseKeyException()
            return LocalDbKey(projectId, decode(key, DEFAULT))
        } catch (t: Throwable) {
            if (t is MissingLocalDatabaseKeyException) {
                migrateFromUnsecuredKey(projectId)?.let {
                    return it
                }
            }

            throw t
        }
    }

    private fun migrateFromUnsecuredKey(projectId: String):LocalDbKey? {
        try {
            val legacyLocalDbKey = unsecuredLocalDbKeyProvider.getLocalDbKeyOrThrow(projectId)
            val legacyRealmKey = encodeToString(legacyLocalDbKey.value, DEFAULT)
            encryptedSharedPrefs.edit().putString(getSharedPrefsKeyForRealm(projectId), legacyRealmKey).apply()
            return legacyLocalDbKey
        } catch (t: Throwable) {
            Timber.e(t)
        }

        return null
    }

    private fun getSharedPrefsKeyForRealm(projectId: String) = "${REALM_KEY}_$projectId"

    private fun readRealmKeyFromSharedPrefs(projectId: String):String? {
        val sharedPrefsKeyForRealm = getSharedPrefsKeyForRealm(projectId)
        return encryptedSharedPrefs.getString(sharedPrefsKeyForRealm, null)
    }

    private fun generateRealmKey(): String {
        val realmKey = randomGenerator.generateByteArray(64)
        return encodeToString(realmKey, DEFAULT)
    }
}
