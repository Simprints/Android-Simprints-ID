package com.simprints.id.data.db.subjects_sync.up.domain

import com.simprints.id.data.db.subjects_sync.up.local.DbSubjectsUpSyncOperation
import com.simprints.id.data.db.subjects_sync.up.local.DbUpSyncOperationKey

data class SubjectsUpSyncOperation(val projectId: String,
                                   val lastResult: SubjectsUpSyncOperationResult?)


fun SubjectsUpSyncOperation.fromDbToDomain() =
    DbSubjectsUpSyncOperation(
        DbUpSyncOperationKey(projectId),
        projectId,
        lastResult?.lastState,
        lastResult?.lastSyncTime)

