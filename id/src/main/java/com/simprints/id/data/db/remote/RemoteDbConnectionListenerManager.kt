package com.simprints.id.data.db.remote

import com.google.firebase.FirebaseApp
import com.simprints.libdata.ConnectionListener


interface RemoteDbConnectionListenerManager {

    var isRemoteConnected: Boolean

    fun registerRemoteConnectionListener(connectionListener: ConnectionListener)
    fun unregisterRemoteConnectionListener(connectionListener: ConnectionListener)
    fun attachConnectionListeners(firebaseApp: FirebaseApp)
    fun detachConnectionListeners(firebaseApp: FirebaseApp)
}
