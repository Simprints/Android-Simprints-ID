package com.simprints.id.data.db.people_sync.down.domain

import com.simprints.id.data.db.people_sync.down.local.DbDownSyncOperation
import com.simprints.id.data.db.people_sync.down.local.DbDownSyncOperationKey
import com.simprints.id.domain.modality.Modes

data class PeopleDownSyncOperation(val projectId: String,
                                   val userId: String?,
                                   val moduleId: String?,
                                   val modes: List<Modes>,
                                   val lastResult: PeopleDownSyncOperationResult?) {
    companion object {

        fun buildProjectOperation(projectId: String,
                                  modes: List<Modes>,
                                  syncOperationResult: PeopleDownSyncOperationResult?) =
            PeopleDownSyncOperation(
                projectId = projectId,
                userId = null,
                moduleId = null,
                modes = modes,
                lastResult = syncOperationResult
            )

        fun buildUserOperation(projectId: String,
                               userId: String,
                               modes: List<Modes>,
                               syncOperationResult: PeopleDownSyncOperationResult?) =
            PeopleDownSyncOperation(
                projectId = projectId,
                userId = userId,
                moduleId = null,
                modes = modes,
                lastResult = syncOperationResult
            )

        fun buildModuleOperation(projectId: String,
                                 moduleId: String,
                                 modes: List<Modes>,
                                 syncOperationResult: PeopleDownSyncOperationResult?) =
            PeopleDownSyncOperation(
                projectId = projectId,
                userId = null,
                moduleId = moduleId,
                modes = modes,
                lastResult = syncOperationResult
            )
    }
}

fun PeopleDownSyncOperation.fromDomainToDb(): DbDownSyncOperation =
    DbDownSyncOperation(
        DbDownSyncOperationKey(projectId, modes, userId, moduleId),
        projectId, userId, moduleId, modes,
        lastResult?.lastState,
        lastResult?.lastPatientId,
        lastResult?.lastPatientUpdatedAt,
        lastResult?.lastSyncTime)
