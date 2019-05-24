package com.simprints.id.data.db.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable
import io.reactivex.Single
import org.jetbrains.anko.doAsync
import timber.log.Timber
import com.simprints.id.domain.Person as LibPerson

open class FirebaseManagerImpl: RemoteDbManager {

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

    override fun isSignedIn(projectId: String, userId: String): Boolean = firebaseAuth.currentUser?.uid?.startsWith(projectId) ?: false

    override fun getCurrentToken(): Single<String> = Single.fromCallable {
            val result = Tasks.await(firebaseAuth.getAccessToken(false))
            result.token?.let {
                it
            } ?: throw RemoteDbNotSignedInException()
        }


    companion object {
        const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5L
    }
}
