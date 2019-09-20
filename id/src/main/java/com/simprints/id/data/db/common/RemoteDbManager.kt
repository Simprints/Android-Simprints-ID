package com.simprints.id.data.db.common

import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import io.reactivex.Completable
import io.reactivex.Single

interface RemoteDbManager {

    fun signIn(token: String): Completable
    fun signOut()

    /** @throws DifferentProjectIdSignedInException */
    fun isSignedIn(projectId: String, userId: String): Boolean

    fun getCurrentToken(): Single<String>
}
