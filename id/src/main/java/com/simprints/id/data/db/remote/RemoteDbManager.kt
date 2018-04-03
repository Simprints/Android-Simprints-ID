package com.simprints.id.data.db.remote

import com.google.firebase.FirebaseApp
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.models.Project
import com.simprints.id.data.db.remote.authListener.RemoteDbAuthListenerManager
import com.simprints.id.data.db.remote.connectionListener.RemoteDbConnectionListenerManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.RemoteApiInterface
import com.simprints.id.exceptions.safe.DifferentCredentialsSignedInException
import com.simprints.id.exceptions.safe.remoteDbManager.DownloadingAPersonWhoDoesntExistOnServer
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.session.Session
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Single

interface RemoteDbManager : RemoteDbConnectionListenerManager, RemoteDbAuthListenerManager {

    // Lifecycle
    fun initialiseRemoteDb()

    fun signInToRemoteDb(tokens: Tokens): Completable
    fun signOutOfRemoteDb()

    fun isRemoteDbInitialized(): Boolean
    @Throws(DifferentCredentialsSignedInException::class)
    fun isSignedIn(projectId: String, userId: String): Boolean

    // Data transfer
    // Firebase
    fun getLocalDbKeyFromRemote(projectId: String): Single<LocalDbKey>

    fun saveIdentificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String)
    fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String)

    fun saveVerificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveRefusalFormInRemote(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String)

    fun saveSessionInRemote(session: Session)

    fun getFirebaseLegacyApp(): FirebaseApp
    fun getCurrentFirestoreToken(): Single<String>

    // API
    fun uploadPerson(fbPerson: fb_Person): Completable
    fun uploadPeople(patientsToUpload: ArrayList<fb_Person>): Completable

    @Throws(DownloadingAPersonWhoDoesntExistOnServer::class)
    fun downloadPerson(patientId: String, projectId: String): Single<fb_Person>

    fun getSyncApi(): Single<RemoteApiInterface>
    fun getNumberOfPatientsForSyncParams(syncParams: SyncTaskParameters): Single<Int>
    fun loadProjectFromRemote(projectId: String): Single<Project>
}
