package com.simprints.id.services.sync

import com.simprints.id.data.DataManager
import com.simprints.id.domain.Constants
import com.simprints.id.services.progress.service.ProgressTaskParameters

sealed class SyncTaskParameters(open val projectId: String, open val moduleId: String?, open val userId: String?) : ProgressTaskParameters {

    companion object {
        fun build(group: Constants.GROUP, dataManager: DataManager): SyncTaskParameters {
            return when (group) {
                Constants.GROUP.GLOBAL -> GlobalSyncTaskParameters(dataManager.getSignedInProjectIdOrEmpty())
                Constants.GROUP.USER -> UserSyncTaskParameters(dataManager.getSignedInProjectIdOrEmpty(), dataManager.getSignedInUserIdOrEmpty())
                Constants.GROUP.MODULE -> ModuleIdSyncTaskParameters(dataManager.getSignedInProjectIdOrEmpty(), dataManager.moduleId)
            }
        }
    }

    data class UserSyncTaskParameters(override val projectId: String,
                                      override val userId: String) : SyncTaskParameters(projectId, null, userId)

    data class ModuleIdSyncTaskParameters(override val projectId: String,
                                          override val moduleId: String) : SyncTaskParameters(projectId, moduleId, null)

    data class GlobalSyncTaskParameters(override val projectId: String) : SyncTaskParameters(projectId, null, null)

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
