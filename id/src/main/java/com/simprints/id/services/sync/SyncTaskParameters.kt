package com.simprints.id.services.sync

import androidx.work.workDataOf
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.Constants

sealed class SyncTaskParameters(open val projectId: String, open val moduleIds: Set<String>?, open val userId: String?) {

    companion object {
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "userId"
        const val MODULES_ID_FIELD = "moduleIds" //StopShip: MODULE_IDS?
        const val MODULE_ID_FIELD = "moduleId"

        @JvmStatic fun build(group: Constants.GROUP,
                             moduleIds: Set<String>,
                             loginInfoManager: LoginInfoManager): SyncTaskParameters {
            return when (group) {
                Constants.GROUP.GLOBAL -> GlobalSyncTaskParameters(loginInfoManager.getSignedInProjectIdOrEmpty())
                Constants.GROUP.USER -> UserSyncTaskParameters(loginInfoManager.getSignedInProjectIdOrEmpty(), loginInfoManager.getSignedInUserIdOrEmpty())
                Constants.GROUP.MODULE -> ModuleIdSyncTaskParameters(loginInfoManager.getSignedInProjectIdOrEmpty(), moduleIds)
            }
        }
    }

    data class UserSyncTaskParameters(override val projectId: String,
                                      override val userId: String) : SyncTaskParameters(projectId, null, userId)

    data class ModuleIdSyncTaskParameters(override val projectId: String,
                                          override val moduleIds: Set<String>) : SyncTaskParameters(projectId, moduleIds, null)

    data class GlobalSyncTaskParameters(override val projectId: String) : SyncTaskParameters(projectId, null, null)

    fun toGroup(): Constants.GROUP {
        return when (this) {
            is UserSyncTaskParameters -> Constants.GROUP.USER
            is ModuleIdSyncTaskParameters -> Constants.GROUP.MODULE
            is GlobalSyncTaskParameters -> Constants.GROUP.GLOBAL
        }
    }

    //StopShip is it the right place for that?
    fun toData() =
        workDataOf(
            PROJECT_ID_FIELD to projectId,
            USER_ID_FIELD to userId,
            MODULES_ID_FIELD to moduleIds
        )
}
