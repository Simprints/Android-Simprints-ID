package com.simprints.id.data.db.remote

import com.google.firebase.FirebaseApp
import com.simprints.id.data.analytics.eventData.SessionsRemoteInterface
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.exceptions.safe.data.db.DownloadingAPersonWhoDoesntExistOnServerException
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.secure.models.Tokens
import com.simprints.id.session.Session
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Single

interface RemoteDbManager {

    // Lifecycle
    fun initialiseRemoteDb()

    fun signInToRemoteDb(tokens: Tokens): Completable
    fun signOutOfRemoteDb()

    fun isRemoteDbInitialized(): Boolean

    /** @throws DifferentProjectIdSignedInException */
    fun isSignedIn(projectId: String, userId: String): Boolean

    // Data transfer
    // Firebase

    fun saveIdentificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String)
    fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String)

    fun saveVerificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveRefusalFormInRemote(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String)

    fun saveSessionInRemote(session: Session)

    fun getFirebaseLegacyApp(): FirebaseApp
    fun getCurrentFirestoreToken(): Single<String>
}
