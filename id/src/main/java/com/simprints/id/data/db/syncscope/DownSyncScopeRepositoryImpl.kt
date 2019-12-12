package com.simprints.id.data.db.syncscope

import com.simprints.id.data.db.syncscope.domain.*
import com.simprints.id.data.db.syncscope.local.fromDbToDomain
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncOperationDao
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.modality.toMode
import kotlinx.coroutines.runBlocking

class DownSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                  val preferencesManager: PreferencesManager,
                                  private val downSyncOperationDao: DownSyncOperationDao) : DownSyncScopeRepository {

    override fun getDownSyncScope(): DownSyncScope {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        val modes: List<Modes> = preferencesManager.modalities.map { it.toMode() }

        val possibleUserId: String? = loginInfoManager.getSignedInUserIdOrEmpty()
        val possibleModuleIds: List<String>? = preferencesManager.selectedModules.toList()

        require(projectId.isEmpty()) { "ProjectId required" }
        require(possibleUserId.isNullOrEmpty()) { "Login required" }

        return when (preferencesManager.syncGroup) {
            GROUP.GLOBAL -> {
                ProjectSyncScope(projectId, modes)
            }
            GROUP.USER ->
                possibleUserId?.let {
                    UserSyncScope(projectId, it, modes)
                } ?: throw IllegalArgumentException("UserId required") //TODO: create exception
            GROUP.MODULE ->
                possibleModuleIds?.let {
                    ModuleSyncScope(projectId, possibleModuleIds, modes)
                } ?: throw IllegalArgumentException("ModuleIds required") //TODO: create exception
        }
    }

    //StopShip: consider to make suspend
    override fun getDownSyncOperations(syncScope: DownSyncScope): List<DownSyncOperation> =
        runBlocking {
            with(downSyncOperationDao) {
                when (syncScope) {
                    is ProjectSyncScope ->
                        getDownSyncOperation(syncScope.projectId, syncScope.modes)
                    is UserSyncScope ->
                        getDownSyncOperation(syncScope.projectId, syncScope.modes, userId = syncScope.userId)
                    is ModuleSyncScope ->
                        syncScope.modules.fold(emptyList()) { operations, moduleId ->
                            operations + getDownSyncOperation(syncScope.projectId, syncScope.modes, moduleId = moduleId)
                        }
                }.map { it.fromDbToDomain() }
            }
        }

    override fun insertOrUpdate(syncScopeOperation: DownSyncOperation) =
        runBlocking {
            downSyncOperationDao.insertOrReplaceDownSyncOperation(syncScopeOperation.fromDomainToDb())
        }

    override fun deleteAll() {
        runBlocking {
            downSyncOperationDao.deleteAll()
        }
    }

}
