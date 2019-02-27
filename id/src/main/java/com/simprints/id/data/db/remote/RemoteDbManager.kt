package com.simprints.id.data.db.remote

import com.google.firebase.FirebaseApp
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.secure.models.Tokens
import io.reactivex.Completable
import io.reactivex.Single
import com.simprints.id.domain.fingerprint.Person as LibPerson

interface RemoteDbManager {

    // Lifecycle
    fun initialiseRemoteDb()

    fun signInToRemoteDb(tokens: Tokens): Completable
    fun signOutOfRemoteDb()

    fun isRemoteDbInitialized(): Boolean

    /** @throws DifferentProjectIdSignedInException */
    fun isSignedIn(projectId: String, userId: String): Boolean

    fun getFirebaseLegacyApp(): FirebaseApp

    fun getCurrentFirestoreToken(): Single<String>
}
