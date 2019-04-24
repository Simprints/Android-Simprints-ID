package com.simprints.fingerprint.controllers.core.repository

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.fingerprint.Person
import io.reactivex.Completable
import io.reactivex.Single

class FingerprintDbManagerImpl(private val dbManager: DbManager): FingerprintDbManager {

    override fun loadPerson(projectId: String, verifyGuid: String): Single<PersonFetchResult> =
        dbManager.loadPerson(projectId, verifyGuid)

    override fun savePerson(person: Person): Completable =
        dbManager.savePerson(person)

    override fun loadPeople(group: GROUP): Single<List<Person>> =
        dbManager.loadPeople(group)
}
