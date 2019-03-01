package com.simprints.id.data.db.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.simprints.id.Application
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable
import io.reactivex.Single
import org.jetbrains.anko.doAsync
import timber.log.Timber
import com.simprints.id.domain.fingerprint.Person as LibPerson

open class FirebaseManagerImpl(
    private val appContext: Context
) : RemoteDbManager {

    // FirebaseApp
    private val legacyFirebaseApp: FirebaseApp by lazy {
        initialiseFirebaseApp()
    }

    private fun initialiseFirebaseApp(): FirebaseApp =
        try {
            Timber.d("Trying to initialise Firebase app")
            FirebaseApp.initializeApp(appContext)
        } catch (stateException: IllegalStateException) {
            Timber.d("Firebase app: already initialized")
            FirebaseApp.getInstance()
        } ?: throw Throwable("test") //StopShp

    private fun getFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth =
        FirebaseAuth.getInstance(firebaseApp)

    override fun signInToRemoteDb(token: String): Completable = signInToDb(legacyFirebaseApp, token)

    private fun signInToDb(firebaseApp: FirebaseApp, token: String): Completable =
        Completable.create {
            getFirebaseAuth(firebaseApp).signInWithCustomToken(token).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Firebase Auth signInWithCustomToken for ${firebaseApp.name} successful")
                    it.onComplete()
                } else {
                    Timber.d("Firebase Auth signInWithCustomToken for ${firebaseApp.name} failed: ${task.exception}")
                    it.onError(task.exception as Throwable)
                }
            }
        }

    override fun signOutOfRemoteDb() {
        getFirebaseAuth(legacyFirebaseApp).signOut()
    }

    override fun isSignedIn(projectId: String, userId: String): Boolean = isLegacySignedInUserAsExpected(projectId)

    private fun isLegacySignedInUserAsExpected(projectId: String): Boolean {
        val firebaseUser = getFirebaseAuth(legacyFirebaseApp).currentUser ?: return false

        // Note: LegacyApiKey and LegacyProjectId are the same thing.
        // For legacy reason, the firebase user has the projectId(for new projects) or legacyApiKey
        // (for old projects) as uid. Because the legacyApiKey soon will disappear, we try to map it immediately (CheckLogin)
        // to a projectId and use it for any task. So we store <ProjectId, HashedLegacyApiKey> in the shared prefs.
        // In this case, we need the legacyApiKey so we grab through the Application to avoid injecting
        // it through all methods, so it will be easier to get rid of it.
        val hashedLegacyApiKey = (appContext as Application).loginInfoManager.getHashedLegacyProjectIdForProjectIdOrEmpty(projectId)
        return if (hashedLegacyApiKey.isNotEmpty()) {
            Hasher().hash(firebaseUser.uid) == hashedLegacyApiKey
        } else {
            firebaseUser.uid == projectId
        }
    }

    private fun getFirebaseLegacyApp(): FirebaseApp = legacyFirebaseApp

    override fun getCurrentFirestoreToken(): Single<String> = Single.create {
        getFirebaseLegacyApp().getToken(false).trace("getCurrentFirestoreToken")
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

        fun getLegacyAppName(): String =
            FirebaseApp.DEFAULT_APP_NAME

        fun getFirestoreAppName(): String = "firestore"
    }
}
