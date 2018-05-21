package com.simprints.id.data.secure

import android.util.Base64
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.RandomGeneratorImpl

class SecureDataManagerImpl(private val keystoreManager: KeystoreManager,
                            private val prefsManager: PreferencesManager,
                            private val randomGenerator: RandomGenerator = RandomGeneratorImpl())
    : SecureDataManager {

    companion object {
        private const val PROJECT_ID_ENC_DATA = "ProjectIdEncData"
        const val SHARED_PREFS_KEY_FOR_REALM_KEY = "${PROJECT_ID_ENC_DATA}_realmKey_"
        const val SHARED_PREFS_KEY_FOR_LEGACY_REALM_KEY = "${PROJECT_ID_ENC_DATA}_legacyRealmKey_"
    }

    override fun setLocalDatabaseKey(projectId: String, legacyApiKey: String?) {
        getSharedKeyForProjectId(SHARED_PREFS_KEY_FOR_REALM_KEY, projectId).let {
            val possibleEncRealmKey = prefsManager.getSharedPreference(it, "")
            if (possibleEncRealmKey.isEmpty()) {
                generateAndSaveRealmKeyInSharedPrefs(projectId)
            }
        }

        legacyApiKey?.let {
            generateAndSaveLegacyRealmKeyInSharedPrefs(projectId, it)
        }
    }

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey {
        val realmKey = readFromSharedPrefsAndDecrypt(SHARED_PREFS_KEY_FOR_REALM_KEY, projectId)
            ?: throw IllegalStateException("Missing local database key")

        val possibleLegacyRealmKey = readFromSharedPrefsAndDecrypt(SHARED_PREFS_KEY_FOR_LEGACY_REALM_KEY, projectId)
        return LocalDbKey(projectId, Base64.decode(realmKey, Base64.DEFAULT) , possibleLegacyRealmKey ?: "")
    }

    private fun generateAndSaveLegacyRealmKeyInSharedPrefs(projectId: String, legacyApiKey: String) {
        val encLegacyRealmKey = keystoreManager.encryptString(legacyApiKey)
        val sharedPrefKeyForLegacyRealmKey = getSharedKeyForProjectId(SHARED_PREFS_KEY_FOR_LEGACY_REALM_KEY, projectId)
        prefsManager.setSharedPreference(sharedPrefKeyForLegacyRealmKey, encLegacyRealmKey)
    }

    private fun generateAndSaveRealmKeyInSharedPrefs(projectId: String) {
        val randomKey = randomGenerator.generateByteArray(64, projectId.toByteArray())
        val realmKey = keystoreManager.encryptString(Base64.encodeToString(randomKey, Base64.DEFAULT))

        val sharedPrefKeyForRealmKey = getSharedKeyForProjectId(SHARED_PREFS_KEY_FOR_REALM_KEY, projectId)
        prefsManager.setSharedPreference(sharedPrefKeyForRealmKey, realmKey)
    }

    private fun getSharedKeyForProjectId(key: String, projectId: String) = key + projectId

    private fun readFromSharedPrefsAndDecrypt(sharedPrefsKey: String, projectId: String): String? {
        val sharedPrefKeyForRealmKey = getSharedKeyForProjectId(sharedPrefsKey, projectId)
        val encryptedSharePrefValue = prefsManager.getSharedPreference(sharedPrefKeyForRealmKey, "")
        return if (encryptedSharePrefValue.isNotEmpty()) {
            keystoreManager.decryptString(encryptedSharePrefValue)
        } else {
            return null
        }
    }
}
