package com.simprints.id.data.db.syncinfo.domain

import com.simprints.id.data.db.syncinfo.local.DbDownSyncOperation
import com.simprints.id.domain.modality.Modes

abstract class DownSyncInfo(open val lastState: DownSyncState,
                            open val lastPatientId: String,
                            open val lastPatientUpdatedAt: Long,
                            open val lastSyncTime: Long? = null) {

    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        ERROR
    }
}

class DownSyncOperation(val projectId: String,
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
        projectId,
        projectId, userId, moduleId, modes,
        syncInfo?.lastState,
        syncInfo?.lastPatientId,
        syncInfo?.lastPatientUpdatedAt,
        syncInfo?.lastSyncTime)
