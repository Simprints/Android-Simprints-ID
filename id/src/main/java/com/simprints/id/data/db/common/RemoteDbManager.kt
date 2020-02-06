package com.simprints.id.data.db.common

import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import io.reactivex.Completable

interface RemoteDbManager {

    fun signIn(token: String): Completable
    fun signOut()

    /** @throws DifferentProjectIdSignedInException */
    fun isSignedIn(projectId: String, userId: String): Boolean

    suspend fun getCurrentToken(): String
}
