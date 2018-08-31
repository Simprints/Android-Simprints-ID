package com.simprints.id.services.sync

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.Constants
import com.simprints.id.services.progress.service.ProgressTaskParameters

sealed class SyncTaskParameters(open val projectId: String, open val moduleId: String?, open val userId: String?) : ProgressTaskParameters {

    companion object {
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "userId"
        const val MODULE_ID_FIELD = "moduleId"

        @JvmStatic fun build(group: Constants.GROUP,
                             moduleId: String,
                             loginInfoManager: LoginInfoManager): SyncTaskParameters {
            return when (group) {
                Constants.GROUP.GLOBAL -> GlobalSyncTaskParameters(loginInfoManager.getSignedInProjectIdOrEmpty())
                Constants.GROUP.USER -> UserSyncTaskParameters(loginInfoManager.getSignedInProjectIdOrEmpty(), loginInfoManager.getSignedInUserIdOrEmpty())
                Constants.GROUP.MODULE -> ModuleIdSyncTaskParameters(loginInfoManager.getSignedInProjectIdOrEmpty(), moduleId)
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
        val map = mutableMapOf(PROJECT_ID_FIELD to projectId)
        moduleId?.let { map[MODULE_ID_FIELD] = it }
        userId?.let { map[USER_ID_FIELD] = it }
        return map
    }
}
