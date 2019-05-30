package com.simprints.id.data.db.remote

import com.auth0.jwt.JWT
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import com.simprints.id.domain.Person as LibPerson

open class FirebaseManagerImpl : RemoteDbManager {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun signInToRemoteDb(token: String): Completable =
        Completable.fromAction {
            val result = Tasks.await(firebaseAuth.signInWithCustomToken(token))
            Timber.d(result.user.uid)
        }

    override fun signOutOfRemoteDb() {
        firebaseAuth.signOut()
    }

    override fun isSignedIn(projectId: String, userId: String): Boolean =
        (JWT.decode(getCurrentToken()
            .subscribeOn(Schedulers.io())
            .blockingGet())
            .claims[projectIdClaim]?.asString() == projectId)

    override fun getCurrentToken(): Single<String> = Single.fromCallable {
        val result = Tasks.await(firebaseAuth.getAccessToken(false))
        result.token?.let {
            it
        } ?: throw RemoteDbNotSignedInException()
    }


    companion object {
        private const val projectIdClaim = "projectId"
        const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5L
    }
}
