package com.simprints.id.data.db.remote.authListener

import com.google.firebase.auth.FirebaseAuth


interface RemoteDbAuthListenerManager {

    val isSignedIn: Boolean

    fun registerRemoteAuthListener(authListener: AuthListener)
    fun unregisterRemoteAuthListener(authListener: AuthListener)
    fun attachAuthListeners(firebaseAuth: FirebaseAuth)
    fun detachAuthListeners(firebaseAuth: FirebaseAuth)
}
