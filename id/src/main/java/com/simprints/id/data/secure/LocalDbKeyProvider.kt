package com.simprints.id.data.secure

import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException

interface LocalDbKeyProvider {

    /**
     * @throws MissingLocalDatabaseKeyException
     **/
    fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
}
