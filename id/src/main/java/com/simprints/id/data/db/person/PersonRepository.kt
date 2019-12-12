package com.simprints.id.data.db.person

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.down_sync_info.domain.DownSyncScope
import com.simprints.id.data.db.down_sync_info.domain.PeopleCount
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource

interface PersonRepository : PersonLocalDataSource, PersonRemoteDataSource, FingerprintIdentityLocalDataSource {

    suspend fun countToDownSync(downSyncScope: DownSyncScope): List<PeopleCount>
    suspend fun localCountForSyncScope(downSyncScope: DownSyncScope): List<PeopleCount>

    suspend fun saveAndUpload(person: Person)
    suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult
}
