package com.simprints.id.data.db.people_sync.up.domain

import com.simprints.id.data.db.people_sync.up.local.DbUpSyncOperation
import com.simprints.id.data.db.people_sync.up.local.DbUpSyncOperationKey

data class PeopleUpSyncOperation(val projectId: String,
                                 val lastResult: PeopleUpSyncOperationResult?)


fun PeopleUpSyncOperation.fromDbToDomain() =
    DbUpSyncOperation(
        DbUpSyncOperationKey(projectId),
        projectId,
        lastResult?.lastSyncTime)

