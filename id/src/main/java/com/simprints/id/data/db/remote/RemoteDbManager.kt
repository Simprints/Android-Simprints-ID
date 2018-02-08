package com.simprints.id.data.db.remote

import com.simprints.id.data.models.Session
import com.simprints.libcommon.Person
import com.simprints.libdata.AuthListener
import com.simprints.libdata.ConnectionListener
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification

interface RemoteDbManager {

    var isRemoteConnected: Boolean

    fun recoverLocalDbSendToRemote(deviceId: String, group: Constants.GROUP, callback: DataCallback)

    fun registerRemoteAuthListener(authListener: AuthListener)
    fun unregisterRemoteAuthListener(authListener: AuthListener)
    fun registerRemoteConnectionListener(connectionListener: ConnectionListener)
    fun unregisterRemoteConnectionListener(connectionListener: ConnectionListener)

    fun saveIdentificationInRemote(probe: Person, matchSize: Int, matches: List<Identification>, sessionId: String): Boolean
    fun savePersonInRemote(person: Person): Boolean
    fun saveRefusalFormInRemote(refusalForm: RefusalForm, sessionId: String): Boolean
    fun saveVerificationInRemote(probe: Person, patientId: String, match: Verification?, sessionId: String,
                         guidExistsResult: VERIFY_GUID_EXISTS_RESULT): Boolean
    fun loadPersonFromRemote(destinationList: MutableList<Person>, guid: String, callback: DataCallback)

    fun updateIdentificationInRemote(apiKey: String, selectedGuid: String, deviceId: String,
                             sessionId: String)

    fun saveSessionInRemote(session: Session)

    fun isRemoteDbInitialized(): Boolean
    fun initializeRemoteDb(callback: DataCallback)
    fun signInToRemote()
    fun finishRemoteDb()

    fun getLocalDbKeyFromRemote(): String

}
