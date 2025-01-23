package com.simprints.infra.security.keyprovider

import android.util.Base64.DEFAULT
import android.util.Base64.decode
import android.util.Base64.encodeToString
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.DB_CORRUPTION
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.exceptions.MatchingLocalDatabaseKeyHashesException
import com.simprints.infra.security.exceptions.MismatchingLocalDatabaseKeyHashesException
import com.simprints.infra.security.exceptions.MissingLocalDatabaseKeyException
import com.simprints.infra.security.exceptions.MissingLocalDatabaseKeyHashException
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider.Companion.FILENAME_FOR_KEY_HASHES_SHARED_PREFS
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider.Companion.FILENAME_FOR_REALM_KEY_SHARED_PREFS
import com.simprints.infra.security.random.RandomGenerator
import com.simprints.infra.security.random.RandomGeneratorImpl
import java.security.MessageDigest
import javax.inject.Inject

internal class SecureLocalDbKeyProviderImpl @Inject constructor(
    private val encryptedSharedPrefs: EncryptedSharedPreferencesBuilder,
    private val randomGenerator: RandomGenerator = RandomGeneratorImpl(),
) : SecureLocalDbKeyProvider {
    companion object {
        // The value of this const can't be changed, otherwise we will need to migrate the previous
        // value.
        private const val SHARED_PREFS_KEY_FOR_DB_KEY_IDENTIFIER = "REALM_KEY"
    }

    private fun createLocalDatabaseKey(dbName: String) {
        val key = generateRealmKey()
        writeRealmKeyInSharedPrefs(dbName, key)

        // Generate and save a hash of the key in a separate file for debugging purposes
        val keyHash = calculateKeyHash(key)
        writeKeyHashInSharedPrefs(dbName, keyHash)
    }

    private fun calculateKeyHash(key: String): String {
        val md = MessageDigest.getInstance("SHA-512")
        return md
            .digest(key.toByteArray())
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    override fun createLocalDatabaseKeyIfMissing(dbName: String) {
        val key = readRealmKeyFromSharedPrefs(dbName)
        if (key == null) {
            createLocalDatabaseKey(dbName)
        }
    }

    /**
     * We are only recreating the DB key if we detect a corruption (either with DB file or key)
     * So, here we log whether hashes are present and match in order to deduce if it was the key
     * that got corrupted
     */
    override fun recreateLocalDatabaseKey(dbName: String) {
        val oldKey = readRealmKeyFromSharedPrefs(dbName)
        if (oldKey == null) {
            logToCrashReport(MissingLocalDatabaseKeyException())
        } else {
            val savedKeyHash = readKeyHashFromSharedPrefs(dbName)
            if (savedKeyHash != null) {
                val oldKeyHash = calculateKeyHash(oldKey)
                if (oldKeyHash != savedKeyHash) {
                    logToCrashReport(MismatchingLocalDatabaseKeyHashesException())
                } else {
                    logToCrashReport(MatchingLocalDatabaseKeyHashesException())
                }
            } else {
                logToCrashReport(MissingLocalDatabaseKeyHashException())
            }
        }

        createLocalDatabaseKey(dbName)
    }

    private fun logToCrashReport(t: Throwable) {
        Simber.e("Failed to recreate local database", t, tag = DB_CORRUPTION)
    }

    /**
     * If you need to open the database using Realm Studio, you can use the following method to
     * decrypt the key.
     *
     * BigInteger(1, Base64.decode(key, Base64.DEFAULT)).toString(16)
     */
    override fun getLocalDbKeyOrThrow(dbName: String): LocalDbKey {
        val key = readRealmKeyFromSharedPrefs(dbName) ?: throw MissingLocalDatabaseKeyException()

        saveKeyHashIfMissing(dbName, key)

        return LocalDbKey(dbName, decode(key, DEFAULT))
    }

    /**
     * If key was created before this update, its hash won't be present in the dedicated shared
     * prefs. In order to cover more users, check if the hash is present at read time and save it
     * if it's missing.
     */
    private fun saveKeyHashIfMissing(
        dbName: String,
        key: String,
    ) {
        val savedKeyHash = readKeyHashFromSharedPrefs(dbName)
        if (savedKeyHash == null) {
            val keyHash = calculateKeyHash(key)
            writeKeyHashInSharedPrefs(dbName, keyHash)
        }
    }

    private fun getSharedPrefsKeyForDbName(dbName: String) = "${SHARED_PREFS_KEY_FOR_DB_KEY_IDENTIFIER}_$dbName"

    private fun writeRealmKeyInSharedPrefs(
        dbName: String,
        key: String,
    ) {
        encryptedSharedPrefs
            .buildEncryptedSharedPreferences(FILENAME_FOR_REALM_KEY_SHARED_PREFS)
            .edit()
            .putString(getSharedPrefsKeyForDbName(dbName), key)
            .apply()
    }

    private fun readRealmKeyFromSharedPrefs(dnName: String): String? = encryptedSharedPrefs
        .buildEncryptedSharedPreferences(FILENAME_FOR_REALM_KEY_SHARED_PREFS)
        .getString(getSharedPrefsKeyForDbName(dnName), null)

    private fun writeKeyHashInSharedPrefs(
        dbName: String,
        keyHash: String,
    ) = encryptedSharedPrefs
        .buildEncryptedSharedPreferences(FILENAME_FOR_KEY_HASHES_SHARED_PREFS)
        .edit()
        .putString(getSharedPrefsKeyForDbName(dbName), keyHash)
        .apply()

    private fun readKeyHashFromSharedPrefs(dnName: String): String? = encryptedSharedPrefs
        .buildEncryptedSharedPreferences(FILENAME_FOR_KEY_HASHES_SHARED_PREFS)
        .getString(getSharedPrefsKeyForDbName(dnName), null)

    private fun generateRealmKey(): String {
        val realmKey = randomGenerator.generateByteArray()
        return encodeToString(realmKey, DEFAULT)
    }
}
