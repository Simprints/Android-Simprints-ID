package com.simprints.id.data.db.down_sync_info

import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperation
import com.simprints.id.data.db.down_sync_info.domain.DownSyncScope

interface DownSyncScopeRepository {

    fun getDownSyncScope(): DownSyncScope

    suspend fun getDownSyncOperations(syncScope: DownSyncScope): List<DownSyncOperation>

    suspend fun insertOrUpdate(syncScopeOperation: DownSyncOperation)

    fun deleteAll()
}
