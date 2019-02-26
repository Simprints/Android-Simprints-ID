package com.simprints.id.data.db.remote

import com.google.firebase.FirebaseApp
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.domain.identification.IdentificationResult
import com.simprints.id.domain.identification.VerificationResult
import com.simprints.id.domain.refusal_form.IdRefusalForm
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.secure.models.Tokens
import com.simprints.id.session.Session
import io.reactivex.Completable
import io.reactivex.Single
import com.simprints.id.domain.fingerprint.Person as LibPerson

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

    fun saveIdentificationInRemote(probe: LibPerson, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<IdentificationResult>, sessionId: String)
    fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String)

    fun saveVerificationInRemote(probe: LibPerson, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: VerificationResult?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveRefusalFormInRemote(refusalForm: IdRefusalForm, projectId: String, userId: String, sessionId: String)

    fun saveSessionInRemote(session: Session)

    fun getFirebaseLegacyApp(): FirebaseApp

    fun getCurrentFirestoreToken(): Single<String>
}
