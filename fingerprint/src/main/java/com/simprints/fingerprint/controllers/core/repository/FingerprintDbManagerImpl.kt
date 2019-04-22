package com.simprints.fingerprint.controllers.core.repository

import com.simprints.id.data.db.DbManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.fingerprint.Person
import io.reactivex.Single

class FingerprintDbManagerImpl(private val dbManager: DbManager): FingerprintDbManager {

    override fun loadPeople(group: GROUP): Single<List<Person>> = dbManager.loadPeople(group)
}
