package com.simprints.fingerprint.controllers.core.repository

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.fingerprint.Person
import io.reactivex.Completable
import io.reactivex.Single

interface FingerprintDbManager {
    fun loadPeople(group: GROUP): Single<List<Person>>
    fun loadPerson(projectId: String, verifyGuid: String): Single<PersonFetchResult>
    fun savePerson(person: Person): Completable
}
