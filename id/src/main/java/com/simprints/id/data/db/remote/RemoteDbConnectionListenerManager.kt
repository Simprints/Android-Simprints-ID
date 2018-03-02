package com.simprints.id.data.db.remote

import com.google.firebase.FirebaseApp
import com.simprints.id.libdata.ConnectionListener


interface RemoteDbConnectionListenerManager {

    val isRemoteConnected: Boolean

    fun registerRemoteConnectionListener(connectionListener: ConnectionListener)
    fun unregisterRemoteConnectionListener(connectionListener: ConnectionListener)
    fun attachConnectionListeners(firebaseApp: FirebaseApp)
    fun detachConnectionListeners(firebaseApp: FirebaseApp)
}
