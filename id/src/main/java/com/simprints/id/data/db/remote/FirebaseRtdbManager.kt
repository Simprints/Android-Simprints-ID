package com.simprints.id.data.db.remote

import com.simprints.libdata.AuthListener
import com.simprints.libdata.ConnectionListener
import com.simprints.libdata.DatabaseContext

class FirebaseRtdbManager: RemoteDbManager {

    override fun isConnected(dbContext: DatabaseContext): Boolean =
            dbContext.isConnected

    override fun registerAuthListener(dbContext: DatabaseContext, authListener: AuthListener) =
            dbContext.registerAuthListener(authListener)

    override fun unregisterAuthListener(dbContext: DatabaseContext, authListener: AuthListener) =
            dbContext.unregisterAuthListener(authListener)

    override fun registerConnectionListener(dbContext: DatabaseContext, connectionListener: ConnectionListener) =
            dbContext.registerConnectionListener(connectionListener)

    override fun unregisterConnectionListener(dbContext: DatabaseContext, connectionListener: ConnectionListener) =
            dbContext.unregisterConnectionListener(connectionListener)

    override fun updateIdentification(apiKey: String, selectedGuid: String, deviceId: String, sessionId: String): Boolean =
            DatabaseContext.updateIdentification(apiKey, selectedGuid, deviceId, sessionId)
}