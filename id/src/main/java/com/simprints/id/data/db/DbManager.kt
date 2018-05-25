package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
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

interface DbManager : RemoteDbManager {

    val localDbManager: LocalDbManager
    val remoteDbManager: RemoteDbManager

    // Lifecycle
    fun initialiseDb()

    fun signIn(projectId: String, tokens: Tokens): Completable
    fun signOut()

    fun isDbInitialised(): Boolean

    // Data transfer
    fun savePerson(person: Person, preferencesManager: PreferencesManager, loginInfoManager: LoginInfoManager): Completable
    fun savePerson(fbPerson: fb_Person): Completable

    fun loadPerson(destinationList: MutableList<Person>, projectId: String, guid: String, callback: DataCallback)

    fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?)

    fun loadProject(projectId: String): Single<Project>

    fun refreshProjectInfoWithServer(projectId: String): Single<Project>

    fun getPeopleCount(personId: String? = null,
                       projectId: String? = null,
                       userId: String? = null,
                       moduleId: String? = null,
                       toSync: Boolean? = null): Single<Int>

    fun calculateNPatientsToDownSync(nPatientsOnServerForSyncParam: Int, syncParams: SyncTaskParameters): Single<Int>

    fun saveIdentification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String)

    fun saveVerification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveSession(session: Session)

    fun saveRefusalForm(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String)

    fun sync(parameters: SyncTaskParameters, interrupted: () -> Boolean): Observable<Progress>

    fun recoverLocalDb(projectId: String, userId: String, androidId: String, moduleId: String, group: Constants.GROUP): Completable
}
