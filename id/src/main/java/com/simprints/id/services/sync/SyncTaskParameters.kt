package com.simprints.id.services.sync

import com.simprints.id.domain.Constants
import com.simprints.id.services.progress.service.ProgressTaskParameters

sealed class SyncTaskParameters(open val projectId: String, open val moduleId: String?, open val userId: String?) : ProgressTaskParameters {

    data class UserSyncTaskParameters(override val projectId: String,
                                      override val userId: String): SyncTaskParameters(projectId, userId, null)

    data class ModuleIdSyncTaskParameters(override val projectId: String,
                                          override val moduleId: String): SyncTaskParameters(projectId, null, moduleId)

    data class GlobalSyncTaskParameters(override val projectId: String): SyncTaskParameters(projectId, null, null)

    fun toGroup(): Constants.GROUP {
        return when (this) {
            is UserSyncTaskParameters -> Constants.GROUP.USER
            is ModuleIdSyncTaskParameters -> Constants.GROUP.MODULE
            is GlobalSyncTaskParameters -> Constants.GROUP.GLOBAL
        }
    }

    fun toMap(): Map<String, String> {
        val map = mutableMapOf("projectId" to projectId)
        moduleId?.let { map["moduleId"] = it }
        userId?.let { map["userId"] = it }
        return map
    }
}
