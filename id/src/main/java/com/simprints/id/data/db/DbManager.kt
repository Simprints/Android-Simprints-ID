package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.models.Session
import com.simprints.id.secure.models.Token
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

interface DbManager : LocalDbManager, RemoteDbManager {

    // Lifecycle
    fun initialiseDb(projectId: String)
    fun signIn(projectId: String, token: Token)
    fun signOut(projectId: String)

    fun isDbInitialised(projectId: String): Boolean

    // Data transfer
    fun savePerson(fbPerson: fb_Person, projectId: String)
    fun loadPerson(destinationList: MutableList<Person>, guid: String, callback: DataCallback)
    fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?)
    fun getPeopleCount(group: Constants.GROUP, userId: String, moduleId: String): Long

    fun saveIdentification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String)

    fun saveVerification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveSession(session: Session)

    fun saveRefusalForm(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String)

    fun syncGlobal(projectId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>)
    fun syncUser(projectId: String, userId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>)

    fun recoverLocalDb(projectId: String, userId: String, androidId: String, moduleId: String, group: Constants.GROUP, callback: DataCallback)
}
