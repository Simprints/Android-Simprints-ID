package com.simprints.id.data.db.common

import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException

interface RemoteDbManager {

    suspend fun signIn(token: String)
    fun signOut()

    /** @throws DifferentProjectIdSignedInException */
    fun isSignedIn(projectId: String, userId: String): Boolean

    suspend fun getCurrentToken(): String
}
