package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.domain.PeopleCount
import com.simprints.id.domain.Project
import com.simprints.id.secure.models.Token
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Completable
import io.reactivex.Single

interface DbManager {

    val personLocalDataSource: PersonLocalDataSource
    val local: LocalDbManager
    val remote: RemoteDbManager
    val personRemoteDataSource: PersonRemoteDataSource
    val remoteProjectManager: RemoteProjectManager

    fun signIn(projectId: String, userId: String, token: Token): Completable
    fun signOut()

    // Data transfer
    fun savePerson(person: Person): Completable

    fun loadPerson(projectId:String, guid: String): Single<PersonFetchResult>

    fun loadPeople(projectId:String, userId: String?, moduleId: String?): Single<List<Person>>

    fun loadProject(projectId: String): Single<Project>

    fun refreshProjectInfoWithServer(projectId: String): Single<Project>

    fun getPeopleCountFromLocalForSyncScope(syncScope: SyncScope): Single<List<PeopleCount>>

    fun getPeopleCountToDownSync(syncScope: SyncScope): Single<List<PeopleCount>>

}
