package com.simprints.id.data.db.person

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncProgress
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface PersonRepository : PersonLocalDataSource, PersonRemoteDataSource, FingerprintIdentityLocalDataSource {

    suspend fun countToDownSync(peopleDownSyncScope: PeopleDownSyncScope): PeopleCount

    suspend fun saveAndUpload(person: Person)
    suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult

    suspend fun performUpload(scope: CoroutineScope): ReceiveChannel<PeopleUpSyncProgress>
}
