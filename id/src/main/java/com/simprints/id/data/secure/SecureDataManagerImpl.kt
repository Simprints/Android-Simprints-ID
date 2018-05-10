package com.simprints.id.data.secure

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.keystore.KeystoreManager


class SecureDataManagerImpl(private val keystoreManager: KeystoreManager,
                            private val prefsManager: PreferencesManager)
    : SecureDataManager {

    companion object {
        private const val LOCAL_DATABASE_KEY = "localDatabaseKey"
    }

    override fun setLocalDatabaseKey(localDatabaseKey: String) {
        val encryptedKey = keystoreManager.encryptString(localDatabaseKey)
        prefsManager.setSharedPreference(LOCAL_DATABASE_KEY, encryptedKey)
    }

    override fun getLocalDatabaseKey(): String =
        prefsManager.getSharedPreference(LOCAL_DATABASE_KEY, "").let {
            if (it.isBlank())
                throw IllegalStateException("Missing local database key")

            keystoreManager.decryptString(it)
        }

}
