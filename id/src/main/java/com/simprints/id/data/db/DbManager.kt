package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.domain.Constants
import com.simprints.id.domain.Project
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.session.Session
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DbManager {

    val local: LocalDbManager
    val remote: RemoteDbManager
    val remotePeopleManager: RemotePeopleManager

    // Lifecycle
    fun initialiseDb()

    fun signIn(projectId: String, tokens: Tokens): Completable
    fun signOut()

    fun isDbInitialised(): Boolean

    // Data transfer
    fun savePerson(person: Person): Completable
    fun savePerson(fbPerson: fb_Person): Completable

    fun loadPerson(destinationList: MutableList<Person>, projectId: String, guid: String, callback: DataCallback)

    fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, callback: DataCallback?)

    fun loadProject(projectId: String): Single<Project>

    fun refreshProjectInfoWithServer(projectId: String): Single<Project>

    fun getPeopleCount(group: Constants.GROUP): Single<Int>

    fun updateIdentification(projectId: String, selectedGuid: String, sessionId: String)

    fun saveRefusalForm(refusalForm: RefusalForm)

    fun calculateNPatientsToDownSync(nPatientsOnServerForSyncParam: Int, syncParams: SyncTaskParameters): Single<Int>

    fun saveSession(session: Session)

    fun sync(parameters: SyncTaskParameters, interrupted: () -> Boolean): Observable<Progress>

    fun recoverLocalDb(group: Constants.GROUP): Completable

    fun saveVerification(probe: Person, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>)
}
