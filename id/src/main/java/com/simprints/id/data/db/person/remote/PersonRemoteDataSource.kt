package com.simprints.id.data.db.person.remote

import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.domain.PeopleOperationsParams
import com.simprints.id.data.db.person.domain.Person
import io.reactivex.Completable
import io.reactivex.Single

interface PersonRemoteDataSource {

    fun downloadPerson(patientId: String, projectId: String): Single<Person>
    fun uploadPeople(projectId: String, patientsToUpload: List<Person>): Completable
    fun getDownSyncPeopleCount(projectId: String, peopleOperationsParams: List<PeopleOperationsParams>): Single<List<PeopleCount>>
    suspend fun getPeopleApiClient(): PeopleRemoteInterface
}
