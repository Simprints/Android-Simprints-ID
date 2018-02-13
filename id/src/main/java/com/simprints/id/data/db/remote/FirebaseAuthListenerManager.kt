package com.simprints.id.data.db.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.simprints.libdata.AuthListener
import timber.log.Timber


class FirebaseAuthListenerManager : RemoteDbAuthListenerManager {

    private lateinit var authDispatcher: FirebaseAuth.AuthStateListener
    private var authListeners = mutableSetOf<AuthListener>()

    @Volatile
    override var isSignedIn = false

    override fun registerRemoteAuthListener(authListener: AuthListener) {
        synchronized(authListeners) {
            authListeners.add(authListener)
        }
    }

    override fun unregisterRemoteAuthListener(authListener: AuthListener) {
        synchronized(authListeners) {
            authListeners.remove(authListener)
        }
    }

    override fun applyAuthListeners(firebaseAuth: FirebaseAuth, appName: String) {
        authDispatcher = createAuthStateListener(appName)
        firebaseAuth.addAuthStateListener(authDispatcher)
        Timber.d("Auth state listener set")
    }

    override fun removeAuthListeners(firebaseAuth: FirebaseAuth) {
        firebaseAuth.removeAuthStateListener(authDispatcher)
    }

    private fun createAuthStateListener(appName: String) =
        FirebaseAuth.AuthStateListener {
            val firebaseUser = it.currentUser
            if (firebaseUser != null) {
                if (isFirebaseUserSameAsProjectId(firebaseUser, appName)) {
                    handleAuthStateSignedIn()
                } else {
                    handleAuthStateSignedOutByOtherUser(it)
                }
            } else {
                handleAuthStateSignedOut()
            }
        }

    private fun isFirebaseUserSameAsProjectId(firebaseUser: FirebaseUser, appName: String): Boolean =
        firebaseUser.uid == appName

    private fun handleAuthStateSignedIn() {
        Timber.d("Signed in")
        synchronized(authListeners) {
            for (authListener in authListeners)
                authListener.onSignIn()
        }
        isSignedIn = true
    }

    private fun handleAuthStateSignedOut() {
        Timber.d("Signed out")
        synchronized(authListeners) {
            for (authListener in authListeners)
                authListener.onSignOut()
        }
        isSignedIn = false
    }

    private fun handleAuthStateSignedOutByOtherUser(firebaseAuth: FirebaseAuth) {
        Timber.d("Signed out by other user")
        firebaseAuth.signOut()
        isSignedIn = false
    }
}
