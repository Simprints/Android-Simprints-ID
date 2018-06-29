package com.simprints.id.data.secure

import com.simprints.id.data.db.local.LocalDbKeyProvider
import com.simprints.id.data.db.local.models.LocalDbKey

interface SecureDataManager : LocalDbKeyProvider {

    fun setLocalDatabaseKey(projectId: String, legacyApiKey: String?)

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
}
