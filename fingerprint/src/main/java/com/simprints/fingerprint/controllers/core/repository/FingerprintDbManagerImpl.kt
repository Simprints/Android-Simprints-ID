package com.simprints.fingerprint.controllers.core.repository

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.fingerprint.Person
import io.reactivex.Completable
import io.reactivex.Single

class FingerprintDbManagerImpl(private val dbManager: DbManager): FingerprintDbManager {

    override fun loadPerson(projectId: String, verifyGuid: String): Single<PersonFetchResult> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun savePerson(person: Person): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadPeople(group: GROUP): Single<List<Person>> = dbManager.loadPeople(group)
}
