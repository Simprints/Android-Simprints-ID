package com.simprints.id.data.db.person

import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Single

interface PersonRepository : PersonLocalDataSource, PersonRemoteDataSource {

    fun countToDownSync(syncScope: SyncScope): Single<List<PeopleCount>>
    fun localCountForSyncScope(syncScope: SyncScope): Single<List<PeopleCount>>

    suspend fun saveAndUpload(person: Person)
    suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult
}
