package com.simprints.id.data.db.people_sync.up

import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperation
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncScope

interface PeopleUpSyncScopeRepository {

    fun getUpSyncScope(): PeopleUpSyncScope

    suspend fun getUpSyncOperations(syncScope: PeopleUpSyncScope): List<PeopleUpSyncOperation>

    suspend fun insertOrUpdate(syncScopeOperation: PeopleUpSyncOperation)

    suspend fun deleteAll()
}
