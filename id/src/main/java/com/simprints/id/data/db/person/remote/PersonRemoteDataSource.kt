package com.simprints.id.data.db.person.remote

import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperation
import com.simprints.id.data.db.down_sync_info.domain.PeopleCount
import io.reactivex.Completable
import io.reactivex.Single

interface PersonRemoteDataSource {

    fun downloadPerson(patientId: String, projectId: String): Single<Person>
    fun uploadPeople(projectId: String, patientsToUpload: List<Person>): Completable
    fun getDownSyncPeopleCount(projectId: String, peopleOperationsParams: List<DownSyncOperation>): Single<List<PeopleCount>>
    suspend fun getPeopleApiClient(): PeopleRemoteInterface
}
