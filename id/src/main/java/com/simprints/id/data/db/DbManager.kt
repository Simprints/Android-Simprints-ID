package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.models.Session
import com.simprints.id.secure.models.Tokens
import com.simprints.libcommon.Person
import com.simprints.libcommon.Progress
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.models.firebase.fb_Person
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Emitter
import io.reactivex.Single

interface DbManager : LocalDbManager, RemoteDbManager {

    // Lifecycle
    fun initialiseDb(projectId: String)
    fun signIn(projectId: String, token: Tokens): Single<Unit>
    fun getLocalKeyAndSignInToLocal(projectId: String): Single<Unit>

    fun signOut()

    fun isDbInitialised(): Boolean

    // Data transfer
    fun savePerson(fbPerson: fb_Person, projectId: String)
    fun loadPerson(destinationList: MutableList<Person>, projectId: String, guid: String, callback: DataCallback)
    fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?)
    fun getPeopleCount(group: Constants.GROUP, userId: String, moduleId: String): Long

    fun saveIdentification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String)

    fun saveVerification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveSession(session: Session)

    fun saveRefusalForm(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String)

    fun syncGlobal(legacyApiKey: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>)
    fun syncUser(legacyApiKey: String, userId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>)

    fun recoverLocalDb(projectId: String, userId: String, androidId: String, moduleId: String, group: Constants.GROUP, callback: DataCallback)
}
