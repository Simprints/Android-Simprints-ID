package com.simprints.id.data.db.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.simprints.id.exceptions.unsafe.RemoteAuthListenersAlreadyAttachedError
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
        checkIfAuthDispatcherHasBeenCreatedAlready()
        authDispatcher = createAuthStateListener()
        firebaseAuth.addAuthStateListener(authDispatcher)
        Timber.d("Auth state listener attached")
    }

    override fun detachAuthListeners(firebaseAuth: FirebaseAuth) {
        firebaseAuth.removeAuthStateListener(authDispatcher)
        Timber.d("Auth state listener detached")
    }

    private fun checkIfAuthDispatcherHasBeenCreatedAlready() {
        if (::authDispatcher.isInitialized) {
            throw RemoteAuthListenersAlreadyAttachedError()
        }
    }

    private fun createAuthStateListener() =
        FirebaseAuth.AuthStateListener {
            if (isFirebaseUserSignedIn(it.currentUser)) {
                handleAuthStateSignedIn()
            } else {
                handleAuthStateSignedOut()
            }
        }

    private fun isFirebaseUserSignedIn(firebaseUser: FirebaseUser?) =
        firebaseUser != null

    private fun handleAuthStateSignedIn() {
        Timber.d("Signed in")
        applyToAuthListeners { onSignIn() }
        isSignedIn = true
    }

    private fun handleAuthStateSignedOut() {
        Timber.d("Signed out")
        applyToAuthListeners { onSignOut() }
        isSignedIn = false
    }

    private fun applyToAuthListeners(operation: AuthListener.() -> Unit) {
        synchronized(authListeners) {
            for (authListener in authListeners)
                authListener.operation()
        }
    }
}
