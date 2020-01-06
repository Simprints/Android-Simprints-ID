package com.simprints.id.data.secure

@Deprecated("Use SecureLocalDbKeyProvider")
interface LegacyLocalDbKeyProvider : LocalDbKeyProvider {

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
    fun removeLocalDbKeyForProjectId(projectId: String)
}
