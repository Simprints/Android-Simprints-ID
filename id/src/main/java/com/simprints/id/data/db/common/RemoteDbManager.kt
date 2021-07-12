package com.simprints.id.data.db.common

import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.secure.models.Token

interface RemoteDbManager {

    suspend fun signIn(token: Token)
    fun signOut()

    /** @throws DifferentProjectIdSignedInException */
    fun isSignedIn(projectId: String, userId: String): Boolean

    suspend fun getCurrentToken(): String
}
