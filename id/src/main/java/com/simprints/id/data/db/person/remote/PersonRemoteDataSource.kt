package com.simprints.id.data.db.person.remote

import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.person.domain.Person

interface PersonRemoteDataSource {

    suspend fun downloadPerson(patientId: String, projectId: String): Person
    suspend fun uploadPeople(projectId: String, patientsToUpload: List<Person>)

    suspend fun getDownSyncPeopleCount(projectId: String, peopleOperationsParams: List<PeopleDownSyncOperation>): List<PeopleCount>
    suspend fun getPeopleApiClient(): PeopleRemoteInterface
}
