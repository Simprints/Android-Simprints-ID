package com.simprints.id.data.db.local

import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException

interface LocalDbKeyProvider {

    /**
     * @throws MissingLocalDatabaseKeyException
     **/
    fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
}
