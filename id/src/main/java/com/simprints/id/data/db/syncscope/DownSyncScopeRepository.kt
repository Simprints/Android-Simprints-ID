package com.simprints.id.data.db.syncscope

import com.simprints.id.data.db.syncscope.domain.DownSyncOperation
import com.simprints.id.data.db.syncscope.domain.DownSyncScope

interface DownSyncScopeRepository {

    fun getDownSyncScope(): DownSyncScope

    fun getDownSyncOperations(syncScope: DownSyncScope): List<DownSyncOperation>

    fun insertOrUpdate(syncScopeOperation: DownSyncOperation)
}
