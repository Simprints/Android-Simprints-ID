package com.simprints.id.data.db.remote.connectionListener

import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.simprints.id.exceptions.unsafe.RemoteConnectionListenersAlreadyAttachedError
import com.simprints.libdata.tools.Utils
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean


class FirebaseConnectionListenerManager : RemoteDbConnectionListenerManager {

    override val isRemoteConnected: Boolean
        get() = isRemoteConnectedBackingField.get()
    private val isRemoteConnectedBackingField: AtomicBoolean = AtomicBoolean(false)

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
        checkIfConnectionDispatcherHasBeenCreatedAlready()
        connectionDispatcher = connectionEventListener
        connectionDbRef = Utils.getDatabase(firebaseApp).getReference(connectedNode)
        connectionDbRef.addValueEventListener(connectionDispatcher)
        Timber.d("Connection listener set")
    }

    override fun detachConnectionListeners(firebaseApp: FirebaseApp) {
        connectionDbRef.removeEventListener(connectionDispatcher)
    }

    private val connectionEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            val connected = dataSnapshot.getValue(Boolean::class.java)?: false
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

    private fun checkIfConnectionDispatcherHasBeenCreatedAlready() {
        if (::connectionDispatcher.isInitialized) {
            throw RemoteConnectionListenersAlreadyAttachedError()
        }
    }

    private fun handleConnected() {
        Timber.d("Connected")
        applyToConnectionListeners { onConnection() }
        isRemoteConnectedBackingField.set(true)
    }

    private fun handleDisconnected() {
        Timber.d("Disconnected")
        applyToConnectionListeners { onDisconnection() }
        isRemoteConnectedBackingField.set(false)
    }

    private fun applyToConnectionListeners(operation: ConnectionListener.() -> Unit) {
        synchronized(connectionListeners) {
            for (listener in connectionListeners)
                listener.operation()
        }
    }

    companion object {
        private const val connectedNode = ".info/connected"
    }
}
