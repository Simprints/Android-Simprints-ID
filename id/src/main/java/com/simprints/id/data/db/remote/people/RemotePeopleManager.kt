package com.simprints.id.data.db.remote.people

import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.domain.fingerprint.Person
import io.reactivex.Completable
import io.reactivex.Single

interface RemotePeopleManager {

    fun downloadPerson(patientId: String, projectId: String): Single<Person>
    fun uploadPeople(projectId: String, patientsToUpload: List<Person>): Completable
    fun getNumberOfPatients(projectId: String, userId: String?, moduleId: String?): Single<Int>
    fun getPeopleApiClient(): Single<PeopleRemoteInterface>
}
