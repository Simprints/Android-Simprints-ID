package com.simprints.id.data.secure

import android.content.SharedPreferences
import android.util.Base64.*
import com.simprints.core.exceptions.MissingLocalDatabaseKeyException
import com.simprints.core.security.LocalDbKey
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.RandomGeneratorImpl
import com.simprints.infra.logging.Simber

open class SecureLocalDbKeyProviderImpl(
    private val encryptedSharedPrefs: SharedPreferences,
    private val randomGenerator: RandomGenerator = RandomGeneratorImpl()
) : SecureLocalDbKeyProvider {

    companion object {
        const val SHARED_PREFS_KEY_FOR_REALM_KEY_IDENTIFIER = "REALM_KEY"
    }

    override fun setLocalDatabaseKey(projectId: String) {
        var key = readRealmKeyFromSharedPrefs(projectId)
        if (key.isNullOrBlank()) {
            key = generateRealmKey()
            encryptedSharedPrefs.edit().putString(getSharedPrefsKeyForRealm(projectId), key).apply()
        }
    }

    /**
     * If you need to open the database using Realm Studio, you can use the following method to
     * decrypt the key.
     *
     * BigInteger(1, Base64.decode(key, Base64.DEFAULT)).toString(16)
     */
    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey {
        val key = readRealmKeyFromSharedPrefs(projectId)

        if (key == null) {
            Simber.e(MissingLocalDatabaseKeyException())
            throw MissingLocalDatabaseKeyException()
        }

        return LocalDbKey(projectId, decode(key, DEFAULT))
    }

    private fun getSharedPrefsKeyForRealm(projectId: String) =
        "${SHARED_PREFS_KEY_FOR_REALM_KEY_IDENTIFIER}_$projectId"

    private fun readRealmKeyFromSharedPrefs(projectId: String): String? {
        val sharedPrefsKeyForRealm = getSharedPrefsKeyForRealm(projectId)
        return encryptedSharedPrefs.getString(sharedPrefsKeyForRealm, null)
    }

    private fun generateRealmKey(): String {
        val realmKey = randomGenerator.generateByteArray()
        return encodeToString(realmKey, DEFAULT)
    }
}
