package com.simprints.id.data.db.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.models.Session
import com.simprints.libdata.AuthListener
import com.simprints.libdata.ConnectionListener
import com.simprints.libdata.DatabaseContext
import com.simprints.libdata.models.firebase.fb_Session
import com.simprints.libdata.tools.Routes.sessionRef

class FirebaseRtdbManager: RemoteDbManager {

    // Firebase
    private var firebaseApp: FirebaseApp? = null
    private lateinit var projectRef: DatabaseReference
    private val session: fb_Session? = null

    // Connection listener
    @Volatile
    override var isConnected: Boolean = false
    private lateinit var connectionDbRef: DatabaseReference
    private lateinit var connectionDispatcher: ValueEventListener
    private val connectionListeners = mutableSetOf<ConnectionListener>()

    // Authentication listener
    @Volatile
    private var signedIn = false
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authDispatcher: FirebaseAuth.AuthStateListener
    private val authListeners = mutableSetOf<AuthListener>()

    override fun registerRemoteAuthListener(authListener: AuthListener) {
        return synchronized(authListeners) {
            authListeners.add(authListener)
        }
    }

    override fun unregisterRemoteAuthListener(authListener: AuthListener) {
        return synchronized(authListeners) {
            authListeners.remove(authListener)
        }
    }

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

    override fun updateIdentification(apiKey: String, selectedGuid: String, deviceId: String, sessionId: String) {
        // DatabaseContext.updateIdentification always returns true (erk legacy code)
        // hence why we are not checking its return value
        DatabaseContext.updateIdentification(apiKey, selectedGuid, deviceId, sessionId)
    }

    override fun saveSession(session: Session) {
        val task = sessionRef(firebaseApp).push().setValue(session.toFirebaseSession())
        Tasks.await(task)
    }

    override fun signIn() {
        TODO("not implemented")
    }

    override fun getLocalDbKey(): String {
        TODO("not implemented")
    }

}
