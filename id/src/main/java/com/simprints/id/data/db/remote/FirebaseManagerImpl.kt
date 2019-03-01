package com.simprints.id.data.db.remote

import com.google.firebase.auth.FirebaseAuth
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable
import io.reactivex.Single
import org.jetbrains.anko.doAsync
import timber.log.Timber
import com.simprints.id.domain.fingerprint.Person as LibPerson

open class FirebaseManagerImpl: RemoteDbManager {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun signInToRemoteDb(token: String): Completable =
        Completable.create {
            firebaseAuth.signInWithCustomToken(token).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Firebase Auth signInWithCustomToken successfully")
                    it.onComplete()
                } else {
                    Timber.d("Firebase Auth signInWithCustomToken failed: ${task.exception}")
                    it.onError(task.exception as Throwable)
                }
            }
        }

    override fun signOutOfRemoteDb() {
        firebaseAuth.signOut()
    }

    override fun isSignedIn(projectId: String, userId: String): Boolean = firebaseAuth.currentUser?.uid == projectId

    override fun getCurrentToken(): Single<String> = Single.create {
        firebaseAuth.getAccessToken(false).trace("getCurrentToken")
            .addOnSuccessListener { result ->
                // Firebase callbacks return on main thread, so the emits
                // will be in the same thread.
                doAsync {
                    val token = result.token
                    if (token == null) {
                        it.onError(RemoteDbNotSignedInException())
                    } else {
                        it.onSuccess(token)
                    }
                }
            }
            .addOnFailureListener { e -> it.onError(e) }
    }

    companion object {
        const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5L
    }
}
