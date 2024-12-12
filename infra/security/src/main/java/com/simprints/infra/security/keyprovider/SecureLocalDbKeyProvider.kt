package com.simprints.infra.security.keyprovider

internal interface SecureLocalDbKeyProvider {
    companion object {
        internal const val FILENAME_FOR_REALM_KEY_SHARED_PREFS = "FILENAME_FOR_REALM_KEY_SHARED"
        internal const val FILENAME_FOR_KEY_HASHES_SHARED_PREFS = "FILENAME_FOR_KEY_HASHES_SHARED_PREFS"
    }

    fun createLocalDatabaseKeyIfMissing(dbName: String)

    fun recreateLocalDatabaseKey(dbName: String)

    fun getLocalDbKeyOrThrow(dbName: String): LocalDbKey
}
