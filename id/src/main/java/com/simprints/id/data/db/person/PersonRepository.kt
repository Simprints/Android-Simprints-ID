package com.simprints.id.data.db.person

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.syncscope.domain.PeopleCount
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.syncscope.domain.DownSyncScope
import io.reactivex.Single

interface PersonRepository : PersonLocalDataSource, PersonRemoteDataSource, FingerprintIdentityLocalDataSource {

    suspend fun countToDownSync(syncScope: DownSyncScope): List<PeopleCount>
    fun localCountForSyncScope(syncScope: DownSyncScope): Single<List<PeopleCount>>

    suspend fun saveAndUpload(person: Person)
    suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult
}
