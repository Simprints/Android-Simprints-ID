package com.simprints.core.security

import com.simprints.core.exceptions.MissingLocalDatabaseKeyException

interface LocalDbKeyProvider {

    /**
     * @throws MissingLocalDatabaseKeyException
     **/
    fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
}
