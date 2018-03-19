package com.simprints.id.data.db.remote

import com.google.firebase.FirebaseApp
import com.simprints.id.data.db.DataCallback
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.remote.authListener.RemoteDbAuthListenerManager
import com.simprints.id.data.db.remote.connectionListener.RemoteDbConnectionListenerManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.exceptions.safe.DifferentCredentialsSignedInException
import com.simprints.id.secure.models.Tokens
import com.simprints.id.session.Session
import com.simprints.libcommon.Person
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
    fun getLocalDbKeyFromRemote(projectId: String): Single<LocalDbKey>

    fun savePersonInRemote(fbPerson: fb_Person, projectId: String)
    fun loadPersonFromRemote(destinationList: MutableList<Person>, projectId: String, guid: String, callback: DataCallback)

    fun saveIdentificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String)
    fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String)

    fun saveVerificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveRefusalFormInRemote(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String)

    fun saveSessionInRemote(session: Session)

    fun getFirebaseLegacyApp(): FirebaseApp
    fun getCurrentFirestoreToken(): Single<String>
}
