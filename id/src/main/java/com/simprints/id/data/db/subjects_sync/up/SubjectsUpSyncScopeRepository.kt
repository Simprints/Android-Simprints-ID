package com.simprints.id.data.db.subjects_sync.up

import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncOperation
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncScope

interface SubjectsUpSyncScopeRepository {

    fun getUpSyncScope(): SubjectsUpSyncScope

    suspend fun getUpSyncOperations(syncScope: SubjectsUpSyncScope): List<SubjectsUpSyncOperation>

    suspend fun insertOrUpdate(syncScopeOperation: SubjectsUpSyncOperation)

    suspend fun deleteAll()
}
