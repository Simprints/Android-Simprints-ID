package com.simprints.id.data.db.people_sync.down.domain

import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperationKey
import com.simprints.id.domain.modality.Modes

data class PeopleDownSyncOperation(val projectId: String,
                                   val userId: String?,
                                   val moduleId: String?,
                                   val modes: List<Modes>,
                                   val lastResult: PeopleDownSyncOperationResult?)

fun PeopleDownSyncOperation.fromDomainToDb(): DbPeopleDownSyncOperation =
    DbPeopleDownSyncOperation(
        DbPeopleDownSyncOperationKey(projectId, modes, userId, moduleId),
        projectId, userId, moduleId, modes,
        lastResult?.state,
        lastResult?.lastPatientId,
        lastResult?.lastPatientUpdatedAt,
        lastResult?.lastSyncTime)
