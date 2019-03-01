package com.simprints.id.data.db.remote

import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import io.reactivex.Completable
import io.reactivex.Single
import com.simprints.id.domain.fingerprint.Person as LibPerson

interface RemoteDbManager {

    fun signInToRemoteDb(token: String): Completable
    fun signOutOfRemoteDb()

    /** @throws DifferentProjectIdSignedInException */
    fun isSignedIn(projectId: String, userId: String): Boolean

    fun getCurrentFirestoreToken(): Single<String>
}
