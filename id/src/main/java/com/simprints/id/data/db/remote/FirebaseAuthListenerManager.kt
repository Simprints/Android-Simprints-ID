package com.simprints.id.data.db.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.simprints.id.exceptions.unsafe.RemoteAuthListenersAlreadyAttachedError
import com.simprints.id.libdata.AuthListener
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class FirebaseAuthListenerManager : RemoteDbAuthListenerManager {

    override val isSignedIn: Boolean
        get() = isSignedInBackingField.get()
    private val isSignedInBackingField: AtomicBoolean = AtomicBoolean(false)

    private lateinit var authDispatcher: FirebaseAuth.AuthStateListener
    private var authListeners = mutableSetOf<AuthListener>()

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
        isSignedInBackingField.set(true)
    }

    private fun handleAuthStateSignedOut() {
        Timber.d("Signed out")
        applyToAuthListeners { onSignOut() }
        isSignedInBackingField.set(false)
    }

    private fun applyToAuthListeners(operation: AuthListener.() -> Unit) {
        synchronized(authListeners) {
            for (authListener in authListeners)
                authListener.operation()
        }
    }
}
