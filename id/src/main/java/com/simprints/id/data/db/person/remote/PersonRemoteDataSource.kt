package com.simprints.id.data.db.person.remote

import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleCount
import io.reactivex.Completable
import io.reactivex.Single

interface PersonRemoteDataSource {

    fun downloadPerson(patientId: String, projectId: String): Single<Person>
    fun uploadPeople(projectId: String, patientsToUpload: List<Person>): Completable
    fun getDownSyncPeopleCount(projectId: String, peopleOperationsParams: List<PeopleDownSyncOperation>): Single<List<PeopleCount>>
    suspend fun getPeopleApiClient(): PeopleRemoteInterface
}
