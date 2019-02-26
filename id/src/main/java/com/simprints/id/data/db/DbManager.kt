package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.Project
import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.VerificationResult
import com.simprints.id.domain.refusal_form.IdRefusalForm
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.session.Session
import io.reactivex.Completable
import io.reactivex.Single
import com.simprints.id.domain.fingerprint.Person as LibPerson

interface DbManager {

    val local: LocalDbManager
    val remote: RemoteDbManager
    val remotePeopleManager: RemotePeopleManager
    val remoteProjectManager: RemoteProjectManager

    // Lifecycle
    fun initialiseDb()

    fun signIn(projectId: String, userId: String, tokens: Tokens): Completable
    fun signOut()

    fun isDbInitialised(): Boolean

    // Data transfer
    fun savePerson(person: LibPerson): Completable
    fun savePerson(fbPerson: fb_Person): Completable

    fun loadPerson(destinationList: MutableList<LibPerson>, projectId: String, guid: String, callback: DataCallback)
    fun loadPerson(projectId: String, guid: String): Single<PersonFetchResult>

    fun loadPeople(destinationList: MutableList<LibPerson>, group: GROUP, callback: DataCallback?)

    fun loadProject(projectId: String): Single<Project>

    fun refreshProjectInfoWithServer(projectId: String): Single<Project>

    fun getPeopleCountFromLocalForSyncScope(syncScope: SyncScope): Single<Int>

    fun updateIdentification(projectId: String, selectedGuid: String, sessionId: String)

    fun saveRefusalForm(refusalForm: IdRefusalForm)

    fun calculateNPatientsToDownSync(projectId: String, userId: String?, moduleId: String?): Single<Int>

    fun saveSession(session: Session)

    fun saveVerification(probe: LibPerson, match: VerificationResult?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveIdentification(probe: LibPerson, matchSize: Int, matches: List<IdentificationResult>)
}
