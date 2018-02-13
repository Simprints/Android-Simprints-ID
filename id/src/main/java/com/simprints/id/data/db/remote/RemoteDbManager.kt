package com.simprints.id.data.db.remote

import com.simprints.id.data.models.Session
import com.simprints.id.exceptions.safe.DifferentProjectSignedInException
import com.simprints.id.secure.models.Token
import com.simprints.libcommon.Person
import com.simprints.libdata.AuthListener
import com.simprints.libdata.ConnectionListener
import com.simprints.libdata.DataCallback
import com.simprints.libdata.NaiveSyncManager
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.models.firebase.fb_Person
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification

interface RemoteDbManager {

    // Lifecycle
    fun initialiseRemoteDb(projectId: String)

    fun signInToRemoteDb(projectId: String, token: Token)
    fun signOutOfRemoteDb(projectId: String)

    fun isRemoteDbInitialized(projectId: String): Boolean
    fun isRemoteConnected(): Boolean
    @Throws(DifferentProjectSignedInException::class)
    fun isSignedIn(projectId: String): Boolean

    fun registerRemoteConnectionListener(connectionListener: ConnectionListener)
    fun unregisterRemoteConnectionListener(connectionListener: ConnectionListener)

    // Data transfer
    fun getLocalDbKeyFromRemote(): String

    fun savePersonInRemote(fbPerson: fb_Person, projectId: String)
    fun loadPersonFromRemote(destinationList: MutableList<Person>, guid: String, callback: DataCallback)

    fun saveIdentificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String)
    fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String)

    fun saveVerificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveRefusalFormInRemote(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String)

    fun saveSessionInRemote(session: Session)

    fun getSyncManager(projectId: String): NaiveSyncManager

    fun recoverLocalDbSendToRemote(projectId: String, userId: String, androidId: String, moduleId: String, group: Constants.GROUP, callback: DataCallback)
}
