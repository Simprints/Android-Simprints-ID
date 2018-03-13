package com.simprints.id.services.sync

import com.simprints.id.libdata.tools.Constants
import com.simprints.id.services.progress.service.ProgressTaskParameters

sealed class SyncTaskParameters(open val projectId: String) : ProgressTaskParameters {

    data class UserSyncTaskParameters(override val projectId: String, val userId: String) : SyncTaskParameters(projectId)

    data class ModuleIdSyncTaskParameters(override val projectId: String, val moduleId: String) : SyncTaskParameters(projectId)

    data class GlobalSyncTaskParameters(override val projectId: String) : SyncTaskParameters(projectId)

    fun toGroup(): Constants.GROUP {
        return when (this) {
            is UserSyncTaskParameters -> Constants.GROUP.USER
            is ModuleIdSyncTaskParameters -> Constants.GROUP.MODULE
            is GlobalSyncTaskParameters -> Constants.GROUP.GLOBAL
        }
    }
}
