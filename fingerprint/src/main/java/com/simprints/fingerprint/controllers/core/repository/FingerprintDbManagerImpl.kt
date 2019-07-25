package com.simprints.fingerprint.controllers.core.repository

import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprint.data.domain.person.fromDomainToCore
import com.simprints.id.data.db.DbManager
import io.reactivex.Completable
import io.reactivex.Single

class FingerprintDbManagerImpl(private val dbManager: DbManager): FingerprintDbManager {

    override fun loadPerson(projectId: String, verifyGuid: String): Single<PersonFetchResult> =
        dbManager.loadPerson(projectId, verifyGuid).map {
            PersonFetchResult.fromCoreToDomain(it)
        }

    override fun savePerson(person: Person): Completable =
        dbManager.savePerson(person.fromDomainToCore())

    override fun loadPeople(projectId: String,
                            userId: String?,
                            moduleId: String?): Single<List<Person>> =
        dbManager.loadPeople(projectId, userId, moduleId).map { corePersonList ->
            corePersonList.map { Person.fromCoreToDomain(it) }
        }
}
