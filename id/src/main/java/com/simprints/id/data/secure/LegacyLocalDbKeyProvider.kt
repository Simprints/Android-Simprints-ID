package com.simprints.id.data.secure

@Deprecated("Use SecureLocalDbKeyProvider")
interface LegacyLocalDbKeyProvider : LocalDbKeyProvider {

    fun setLocalDatabaseKey(projectId: String)
    fun removeLocalDbKeyForProjectId(projectId: String)

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey

}
