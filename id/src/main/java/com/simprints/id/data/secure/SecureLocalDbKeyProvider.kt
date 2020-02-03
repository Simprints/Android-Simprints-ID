package com.simprints.id.data.secure

interface SecureLocalDbKeyProvider: LocalDbKeyProvider {

    companion object {
        const val FILENAME_FOR_REALM_KEY_SHARED_PREFS = "FILENAME_FOR_REALM_KEY_SHARED"
    }

    fun setLocalDatabaseKey(projectId: String)

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
}
