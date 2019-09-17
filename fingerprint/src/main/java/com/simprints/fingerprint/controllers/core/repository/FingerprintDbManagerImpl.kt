package com.simprints.fingerprint.controllers.core.repository

import com.simprints.core.tools.extentions.completableWithSuspend
import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprint.data.domain.person.fromDomainToCore
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

class FingerprintDbManagerImpl(private val personRepository: PersonRepository) : FingerprintDbManager {

    override fun loadPerson(projectId: String, verifyGuid: String): Single<PersonFetchResult> =
        singleWithSuspend {
            val corePersonFetch = runBlocking { personRepository.loadFromRemoteIfNeeded(projectId = projectId, patientId = verifyGuid) }
            PersonFetchResult.fromCoreToDomain(corePersonFetch)
        }

    override fun savePerson(person: Person): Completable =
        completableWithSuspend {
            person.toSync = true
            personRepository.saveAndUpload(person.fromDomainToCore())
        }

    override fun loadPeople(projectId: String,
                            userId: String?,
                            moduleId: String?): Single<List<Person>> =
        singleWithSuspend {
            val peopleList = personRepository.load(PersonLocalDataSource.Query(projectId = projectId, userId = userId, moduleId = moduleId))
            peopleList.toList().map { Person.fromCoreToDomain(it) }
        }
}
