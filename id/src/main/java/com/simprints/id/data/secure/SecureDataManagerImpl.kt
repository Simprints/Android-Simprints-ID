package com.simprints.id.data.secure

import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.tools.extensions.toHexString
import java.security.SecureRandom

class SecureDataManagerImpl(private val keystoreManager: KeystoreManager,
                            private val prefsManager: PreferencesManager)
    : SecureDataManager {

    companion object {
        private const val PROJECT_ID_ENC_DATA = "ProjectIdEncData"
        private const val SHARED_PREFS_KEY_FOR_REALM_KEY = "${PROJECT_ID_ENC_DATA}_realmKey_"
        private const val SHARED_PREFS_KEY_FOR_LEGACY_REALM_KEY = "${PROJECT_ID_ENC_DATA}_legacyRealmKey_"
    }

    override fun setLocalDatabaseKey(projectId: String, legacyApiKey: String?) {
        val possibleEncRealmKey = prefsManager.getSharedPreference(getSharedKeyForProjectId(SHARED_PREFS_KEY_FOR_REALM_KEY, projectId), "")
        if (possibleEncRealmKey.isEmpty()) {
            generateAndSaveRealmKeyInSharedPrefs(projectId)
        }

        legacyApiKey?.let {
            generateAndSaveLegacyRealmKeyInSharedPrefs(projectId, it)
        }
    }

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey {
        val realmKey = readFromSharedPrefsAndDecrypt(SHARED_PREFS_KEY_FOR_REALM_KEY, projectId)?.toByteArray()
            ?: throw IllegalStateException("Missing local database key")

        val possibleLegacyRealmKey = readFromSharedPrefsAndDecrypt(SHARED_PREFS_KEY_FOR_REALM_KEY, projectId)
        return LocalDbKey(projectId, realmKey, possibleLegacyRealmKey ?: "")
    }

    private fun generateAndSaveLegacyRealmKeyInSharedPrefs(projectId: String, legacyApiKey: String) {
        val encLegacyRealmKey = keystoreManager.encryptString(legacyApiKey)
        val sharedPrefKeyForLegacyRealmKey = getSharedKeyForProjectId(SHARED_PREFS_KEY_FOR_LEGACY_REALM_KEY, projectId)
        prefsManager.setSharedPreference(sharedPrefKeyForLegacyRealmKey, encLegacyRealmKey)
    }

    private fun generateAndSaveRealmKeyInSharedPrefs(projectId: String) {
        val realmKey = keystoreManager.encryptString(generateRealmKey(projectId).toHexString())
        val sharedPrefKeyForRealmKey = getSharedKeyForProjectId(SHARED_PREFS_KEY_FOR_REALM_KEY, projectId)
        prefsManager.setSharedPreference(sharedPrefKeyForRealmKey, realmKey)
    }

    private fun generateRealmKey(projectId: String): ByteArray {
        val key = ByteArray(32)
        SecureRandom(projectId.toByteArray()).nextBytes(key)
        return key
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
