package com.simprints.id.data.secure

interface SecureLocalDbKeyProvider: LocalDbKeyProvider {

    fun setLocalDatabaseKey(projectId: String)

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
}
