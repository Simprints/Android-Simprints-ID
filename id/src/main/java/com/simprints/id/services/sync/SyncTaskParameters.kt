package com.simprints.id.services.sync

import com.simprints.id.services.progress.service.ProgressTaskParameters

sealed class SyncTaskParameters(open val projectId: String) : ProgressTaskParameters {

    data class UserSyncTaskParameters(override val projectId: String, val userId: String) : SyncTaskParameters(projectId)

    data class ModuleIdSyncTaskParameters(override val projectId: String, val moduleId: String) : SyncTaskParameters(projectId)

    data class GlobalSyncTaskParameters(override val projectId: String) : SyncTaskParameters(projectId)
}
