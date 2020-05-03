package com.simprints.id.data.db.subjects_sync.down

import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncScope

interface SubjectsDownSyncScopeRepository {

    fun getDownSyncScope(): SubjectsDownSyncScope

    suspend fun getDownSyncOperations(syncScope: SubjectsDownSyncScope): List<SubjectsDownSyncOperation>

    suspend fun refreshDownSyncOperationFromDb(opToRefresh: SubjectsDownSyncOperation): SubjectsDownSyncOperation?

    suspend fun insertOrUpdate(syncScopeOperation: SubjectsDownSyncOperation)

    suspend fun deleteAll()
}
