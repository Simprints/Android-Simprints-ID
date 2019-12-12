package com.simprints.id.data.db.syncscope.domain

import com.simprints.id.data.db.syncscope.local.DbDownSyncOperation
import com.simprints.id.domain.modality.Modes

data class DownSyncOperation(val projectId: String,
                             val userId: String?,
                             val moduleId: String?,
                             val modes: List<Modes>,
                             val syncInfo: DownSyncInfo?) {
    companion object {
        fun buildProjectOperation(projectId: String,
                                  modes: List<Modes>,
                                  syncInfo: DownSyncInfo?) =
            DownSyncOperation(
                projectId = projectId,
                userId = null,
                moduleId = null,
                modes = modes,
                syncInfo = syncInfo
            )

        fun buildUserOperation(projectId: String,
                               userId: String,
                               modes: List<Modes>,
                               syncInfo: DownSyncInfo?) =
            DownSyncOperation(
                projectId = projectId,
                userId = userId,
                moduleId = null,
                modes = modes,
                syncInfo = syncInfo
            )

        fun buildModuleOperation(projectId: String,
                                 moduleId: String,
                                 modes: List<Modes>,
                                 syncInfo: DownSyncInfo?) =
            DownSyncOperation(
                projectId = projectId,
                userId = null,
                moduleId = moduleId,
                modes = modes,
                syncInfo = syncInfo
            )
    }
}

fun DownSyncOperation.fromDomainToDb(): DbDownSyncOperation =
    DbDownSyncOperation(
        listOf(projectId, userId, moduleId, modes.joinToString("_")).joinToString("_"),
        projectId, userId, moduleId, modes,
        syncInfo?.lastState,
        syncInfo?.lastPatientId,
        syncInfo?.lastPatientUpdatedAt,
        syncInfo?.lastSyncTime)
