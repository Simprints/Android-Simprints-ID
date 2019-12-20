package com.simprints.id.data.db.people_sync.down

import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope

interface PeopleDownSyncScopeRepository {

    fun getDownSyncScope(): PeopleDownSyncScope

    suspend fun getDownSyncOperations(syncScope: PeopleDownSyncScope): List<PeopleDownSyncOperation>
    suspend fun refreshFromDb(opToRefresh: PeopleDownSyncOperation): PeopleDownSyncOperation?

    suspend fun insertOrUpdate(syncScopeOperation: PeopleDownSyncOperation)

    fun deleteAll()
}
