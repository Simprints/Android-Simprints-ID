package com.simprints.id.data.db.local

import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.exceptions.unsafe.MissingLocalDatabaseKey

interface LocalDbKeyProvider {

    /**
     * @throws MissingLocalDatabaseKey
     **/
    fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
}
