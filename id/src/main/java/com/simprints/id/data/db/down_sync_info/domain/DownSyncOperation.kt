package com.simprints.id.data.db.down_sync_info.domain

import com.simprints.id.data.db.down_sync_info.local.DbDownSyncOperation
import com.simprints.id.data.db.down_sync_info.local.DbDownSyncOperationKey
import com.simprints.id.domain.modality.Modes

data class DownSyncOperation(val projectId: String,
                             val userId: String?,
                             val moduleId: String?,
                             val modes: List<Modes>,
                             val syncOperationResult: DownSyncOperationResult?) {
    companion object {

        fun buildProjectOperation(projectId: String,
                                  modes: List<Modes>,
                                  syncOperationResult: DownSyncOperationResult?) =
            DownSyncOperation(
                projectId = projectId,
                userId = null,
                moduleId = null,
                modes = modes,
                syncOperationResult = syncOperationResult
            )

        fun buildUserOperation(projectId: String,
                               userId: String,
                               modes: List<Modes>,
                               syncOperationResult: DownSyncOperationResult?) =
            DownSyncOperation(
                projectId = projectId,
                userId = userId,
                moduleId = null,
                modes = modes,
                syncOperationResult = syncOperationResult
            )

        fun buildModuleOperation(projectId: String,
                                 moduleId: String,
                                 modes: List<Modes>,
                                 syncOperationResult: DownSyncOperationResult?) =
            DownSyncOperation(
                projectId = projectId,
                userId = null,
                moduleId = moduleId,
                modes = modes,
                syncOperationResult = syncOperationResult
            )
    }
}

fun DownSyncOperation.fromDomainToDb(): DbDownSyncOperation =
    DbDownSyncOperation(
        DbDownSyncOperationKey(projectId, modes, userId, moduleId),
        projectId, userId, moduleId, modes,
        syncOperationResult?.lastState,
        syncOperationResult?.lastPatientId,
        syncOperationResult?.lastPatientUpdatedAt,
        syncOperationResult?.lastSyncTime)
