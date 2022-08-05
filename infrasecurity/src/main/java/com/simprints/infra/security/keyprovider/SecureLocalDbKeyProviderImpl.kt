package com.simprints.infra.security.keyprovider

import android.content.SharedPreferences
import android.util.Base64.*
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.exceptions.MissingLocalDatabaseKeyException
import com.simprints.infra.security.random.RandomGenerator
import com.simprints.infra.security.random.RandomGeneratorImpl
import javax.inject.Inject

internal class SecureLocalDbKeyProviderImpl @Inject constructor(
    private val encryptedSharedPrefs: SharedPreferences,
    private val randomGenerator: RandomGenerator = RandomGeneratorImpl()
) : SecureLocalDbKeyProvider {

    companion object {
        // The value of this const can't be changed, otherwise we will need to migrate the previous
        // value.
        private const val SHARED_PREFS_KEY_FOR_DB_KEY_IDENTIFIER = "REALM_KEY"
    }

    override fun createLocalDatabaseKeyIfMissing(dbName: String) {
        var key = readRealmKeyFromSharedPrefs(dbName)
        if (key == null) {
            key = generateRealmKey()
            encryptedSharedPrefs.edit().putString(getSharedPrefsKeyForDbName(dbName), key).apply()
        }
    }

    /**
     * If you need to open the database using Realm Studio, you can use the following method to
     * decrypt the key.
     *
     * BigInteger(1, Base64.decode(key, Base64.DEFAULT)).toString(16)
     */
    override fun getLocalDbKeyOrThrow(dbName: String): LocalDbKey {
        val key = readRealmKeyFromSharedPrefs(dbName)

        if (key == null) {
            Simber.e(MissingLocalDatabaseKeyException())
            throw MissingLocalDatabaseKeyException()
        }

        return LocalDbKey(dbName, decode(key, DEFAULT))
    }

    private fun getSharedPrefsKeyForDbName(dbName: String) =
        "${SHARED_PREFS_KEY_FOR_DB_KEY_IDENTIFIER}_$dbName"

    private fun readRealmKeyFromSharedPrefs(dnName: String): String? {
        val sharedPrefsKeyForRealm = getSharedPrefsKeyForDbName(dnName)
        return encryptedSharedPrefs.getString(sharedPrefsKeyForRealm, null)
    }

    private fun generateRealmKey(): String {
        val realmKey = randomGenerator.generateByteArray()
        return encodeToString(realmKey, DEFAULT)
    }
}
