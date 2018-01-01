package com.simprints.id.data.db.remote

import com.simprints.id.data.models.Session
import com.simprints.libdata.AuthListener
import com.simprints.libdata.ConnectionListener
import com.simprints.libdata.DatabaseContext

interface RemoteDbManager {

    fun isConnected(dbContext: DatabaseContext): Boolean

    fun registerAuthListener(dbContext: DatabaseContext, authListener: AuthListener)

    fun unregisterAuthListener(dbContext: DatabaseContext, authListener: AuthListener)

    fun registerConnectionListener(dbContext: DatabaseContext,
                                   connectionListener: ConnectionListener)

    fun unregisterConnectionListener(dbContext: DatabaseContext,
                                     connectionListener: ConnectionListener)

    fun updateIdentification(apiKey: String, selectedGuid: String, deviceId: String,
                             sessionId: String)

    fun saveSession(dbContext: DatabaseContext, session: Session)

}
