package com.simprints.fingerprint.controllers.core.repository

import com.simprints.core.tools.extentions.completableWithSuspend
import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.id.data.db.person.local.FingerprintRecordLocalDataSource
import io.reactivex.Completable
import io.reactivex.Single

class FingerprintDbManagerImpl(private val coreFingerprintRecordLocalDataSource: FingerprintRecordLocalDataSource) : FingerprintDbManager {

    //STOPSHIP: To remove - use loadPeople(query: Serializable)
    override fun loadPerson(projectId: String, verifyGuid: String): Single<PersonFetchResult> =
        singleWithSuspend {
            //val corePersonFetch = runBlocking { personRepository.loadFromRemoteIfNeeded(projectId = projectId, patientId = verifyGuid) }
            //PersonFetchResult.fromCoreToDomain(corePersonFetch)
        }

    //STOPSHIP: To be removed after enrolment
    override fun savePerson(person: Person): Completable =
        completableWithSuspend {
            person.toSync = true
            //personRepository.saveAndUpload(person.fromDomainToCore())
        }

    //STOPSHIP: change to loadPeople(query: Serializable). Query is received in the Matching Request
    override fun loadPeople(projectId: String,
                            userId: String?,
                            moduleId: String?): Single<List<Person>> =
        singleWithSuspend {
            //val peopleList = personRepository.load(PersonLocalDataSource.Query(projectId = projectId, userId = userId, moduleId = moduleId))
            //peopleList.toList().map { Person.fromCoreToDomain(it) }
        }
}
