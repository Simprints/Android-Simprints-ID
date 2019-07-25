package com.simprints.id.data.db.remote.people

import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.domain.PeopleCount
import com.simprints.id.domain.Person
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Completable
import io.reactivex.Single

interface RemotePeopleManager {

    fun downloadPerson(patientId: String, projectId: String): Single<Person>
    fun uploadPeople(projectId: String, patientsToUpload: List<Person>): Completable
    fun getDownSyncPeopleCount(syncScope: SyncScope): Single<List<PeopleCount>>
    fun getPeopleApiClient(): Single<PeopleRemoteInterface>
}
