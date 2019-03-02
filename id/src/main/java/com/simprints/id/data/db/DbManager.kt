package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.Project
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Completable
import io.reactivex.Single

interface DbManager {

    val local: LocalDbManager
    val remote: RemoteDbManager
    val remotePeopleManager: RemotePeopleManager
    val remoteProjectManager: RemoteProjectManager

    // Lifecycle
    fun initialiseDb()

    fun signIn(projectId: String, userId: String, tokens: Tokens): Completable
    fun signOut()

    // Data transfer
    fun savePerson(person: Person): Completable

    fun loadPerson(projectId:String, guid: String): Single<PersonFetchResult>

    fun loadPeople(group: GROUP): Single<List<Person>>

    fun loadProject(projectId: String): Single<Project>

    fun refreshProjectInfoWithServer(projectId: String): Single<Project>

    fun getPeopleCountFromLocalForSyncScope(syncScope: SyncScope): Single<Int>

    fun calculateNPatientsToDownSync(projectId: String, userId: String?, moduleId: String?): Single<Int>

}
