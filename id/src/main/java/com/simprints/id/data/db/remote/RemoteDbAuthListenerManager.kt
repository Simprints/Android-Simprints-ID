package com.simprints.id.data.db.remote

import com.google.firebase.auth.FirebaseAuth
import com.simprints.id.libdata.AuthListener


interface RemoteDbAuthListenerManager {

    val isSignedIn: Boolean

    fun registerRemoteAuthListener(authListener: AuthListener)
    fun unregisterRemoteAuthListener(authListener: AuthListener)
    fun attachAuthListeners(firebaseAuth: FirebaseAuth)
    fun detachAuthListeners(firebaseAuth: FirebaseAuth)
}
