package com.simprints.fingerprint.controllers.core.repository

import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.data.domain.person.Person
import io.reactivex.Completable
import io.reactivex.Single
import com.simprints.fingerprint.data.domain.matching.MatchGroup

interface FingerprintDbManager {

    fun loadPeople(group: MatchGroup): Single<List<Person>>
    fun loadPerson(projectId: String, verifyGuid: String): Single<PersonFetchResult>
    fun savePerson(person: Person): Completable
}
