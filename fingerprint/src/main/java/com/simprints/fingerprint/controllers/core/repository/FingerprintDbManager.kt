package com.simprints.fingerprint.controllers.core.repository

import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.data.domain.person.Person
import io.reactivex.Completable
import io.reactivex.Single

interface FingerprintDbManager {

    fun loadPeople(projectId: String,
                   userId: String? = null,
                   moduleId: String? = null): Single<List<Person>>

    fun loadPerson(projectId: String, verifyGuid: String): Single<PersonFetchResult>

    fun savePerson(person: Person): Completable
}
