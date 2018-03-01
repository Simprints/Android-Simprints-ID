package com.simprints.id.data.db.remote

import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.simprints.libdata.ConnectionListener
import com.simprints.libdata.tools.Utils
import timber.log.Timber


class FirebaseConnectionListenerManager : RemoteDbConnectionListenerManager {

    @Volatile
    override var isRemoteConnected = false
    private lateinit var connectionDbRef: DatabaseReference
    private lateinit var connectionDispatcher: ValueEventListener
    private var connectionListeners = mutableSetOf<ConnectionListener>()

    override fun registerRemoteConnectionListener(connectionListener: ConnectionListener) {
        synchronized(connectionListeners) {
            connectionListeners.add(connectionListener)
        }
    }

    override fun unregisterRemoteConnectionListener(connectionListener: ConnectionListener) {
        synchronized(connectionListeners) {
            connectionListeners.remove(connectionListener)
        }
    }

    override fun attachConnectionListeners(firebaseApp: FirebaseApp) {
        connectionDispatcher = connectionEventListener
        connectionDbRef = Utils.getDatabase(firebaseApp).getReference(".info/connected")
        connectionDbRef.addValueEventListener(connectionDispatcher)
        Timber.d("Connection listener set")
    }

    override fun detachConnectionListeners(firebaseApp: FirebaseApp) {
        connectionDbRef.removeEventListener(connectionDispatcher)
    }

    private val connectionEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            val connected = dataSnapshot.getValue(Boolean::class.java)!!
            synchronized(connectionListeners) {
                if (connected) {
                    handleConnected()
                } else {
                    handleDisconnected()
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    }

    private fun handleConnected() {
        Timber.d("Connected")
        isRemoteConnected = true
        for (listener in connectionListeners)
            listener.onConnection()
    }

    private fun handleDisconnected() {
        Timber.d("Disconnected")
        isRemoteConnected = false
        for (listener in connectionListeners)
            listener.onDisconnection()
    }
}
