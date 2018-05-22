package com.simprints.id.data.db.local

import com.simprints.id.data.db.local.models.LocalDbKey

interface LocalDbKeyProvider {

    /**
     * @throws MissingLocalDatabaseKeyException
     **/
    fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
}
