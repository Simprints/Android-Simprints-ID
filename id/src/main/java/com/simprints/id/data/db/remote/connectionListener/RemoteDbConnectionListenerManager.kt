package com.simprints.id.data.db.remote.connectionListener

import com.google.firebase.FirebaseApp


interface RemoteDbConnectionListenerManager {

    val isRemoteConnected: Boolean

    fun registerRemoteConnectionListener(connectionListener: ConnectionListener)
    fun unregisterRemoteConnectionListener(connectionListener: ConnectionListener)
    fun attachConnectionListeners(firebaseApp: FirebaseApp)
    fun detachConnectionListeners(firebaseApp: FirebaseApp)
}
