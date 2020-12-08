package com.simprints.id.data.secure

import android.util.Base64
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException

@Deprecated("Use SecureLocalDbKeyProviderImpl")
open class LegacyLocalDbKeyProviderImpl(private val keystoreManager: KeystoreManager,
                                        private val prefsManager: PreferencesManager)
    : LegacyLocalDbKeyProvider {

    companion object {
        private const val PROJECT_ID_ENC_DATA = "ProjectIdEncData"
        private const val SHARED_PREFS_KEY_FOR_REALM_KEY_IDENTIFIER = "realmKey"
        const val SHARED_PREFS_KEY_FOR_REALM_KEY = "${PROJECT_ID_ENC_DATA}_${SHARED_PREFS_KEY_FOR_REALM_KEY_IDENTIFIER}_"
    }

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey {
        val realmKey = readFromSharedPrefsAndDecrypt(SHARED_PREFS_KEY_FOR_REALM_KEY, projectId)
            ?: throw MissingLocalDatabaseKeyException()

        return LocalDbKey(projectId, Base64.decode(realmKey, Base64.DEFAULT))
    }

    override fun removeLocalDbKeyForProjectId(projectId: String) {
        val projectIdKey = getSharedKeyForRealmKey(SHARED_PREFS_KEY_FOR_REALM_KEY, projectId)
        prefsManager.setSharedPreference(projectIdKey, "")
    }

    private fun getSharedKeyForRealmKey(key: String, projectId: String) = key + projectId

    private fun readFromSharedPrefsAndDecrypt(sharedPrefsKey: String, projectId: String): String? {
        val sharedPrefKeyForRealmKey = getSharedKeyForRealmKey(sharedPrefsKey, projectId)
        val encryptedSharePrefValue = prefsManager.getSharedPreference(sharedPrefKeyForRealmKey, "")
        return if (encryptedSharePrefValue.isNotEmpty()) {
            keystoreManager.decryptString(encryptedSharePrefValue)
        } else {
            return null
        }
    }
}
