package com.simprints.id.data.db.person

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.people_sync.down.domain.PeopleCount
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource

interface PersonRepository : PersonLocalDataSource, PersonRemoteDataSource, FingerprintIdentityLocalDataSource {

    suspend fun countToDownSync(peopleDownSyncScope: PeopleDownSyncScope): List<PeopleCount>

    suspend fun saveAndUpload(person: Person)
    suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult
}
