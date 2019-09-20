package com.simprints.id.data.secure

interface SecureDataManager : LocalDbKeyProvider {

    fun setLocalDatabaseKey(projectId: String)

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
}
