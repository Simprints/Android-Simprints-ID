package com.simprints.id.data.db.remote

import com.google.firebase.auth.FirebaseAuth
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

    override fun attachAuthListeners(firebaseAuth: FirebaseAuth) {
        authDispatcher = createAuthStateListener()
        firebaseAuth.addAuthStateListener(authDispatcher)
        Timber.d("Auth state listener attached")
    }

    override fun detachAuthListeners(firebaseAuth: FirebaseAuth) {
        firebaseAuth.removeAuthStateListener(authDispatcher)
        Timber.d("Auth state listener detached")
    }

    private fun createAuthStateListener() =
        FirebaseAuth.AuthStateListener {
            val firebaseUser = it.currentUser
            if (firebaseUser != null) {
                handleAuthStateSignedIn()
            } else {
                handleAuthStateSignedOut()
            }
        }

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
}
