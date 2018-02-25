package com.simprints.id.data.db.remote

import com.simprints.id.data.models.Session
import com.simprints.id.exceptions.safe.DifferentCredentialsSignedInException
import com.simprints.id.secure.models.Tokens
import com.simprints.libcommon.Person
import com.simprints.libdata.DataCallback
import com.simprints.libdata.NaiveSyncManager
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.models.firebase.fb_Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Single

interface RemoteDbManager : RemoteDbConnectionListenerManager, RemoteDbAuthListenerManager {

    // Lifecycle
    fun initialiseRemoteDb()

    fun signInToRemoteDb(tokens: Tokens): Single<Unit>
    fun signOutOfRemoteDb()

    fun isRemoteDbInitialized(): Boolean
    @Throws(DifferentCredentialsSignedInException::class)
    fun isSignedIn(projectId: String, userId: String): Boolean

    // Data transfer
    fun getLocalDbKeyFromRemote(projectId: String): Single<String>

    fun savePersonInRemote(fbPerson: fb_Person, projectId: String)
    fun loadPersonFromRemote(destinationList: MutableList<Person>, projectId: String, guid: String, callback: DataCallback)

    fun saveIdentificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String)
    fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String)

    fun saveVerificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveRefusalFormInRemote(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String)

    fun saveSessionInRemote(session: Session)

    fun getSyncManager(projectId: String): NaiveSyncManager
}
