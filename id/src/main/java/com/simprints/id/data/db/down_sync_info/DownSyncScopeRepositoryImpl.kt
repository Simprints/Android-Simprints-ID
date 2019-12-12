package com.simprints.id.data.db.down_sync_info

import com.simprints.id.data.db.down_sync_info.domain.*
import com.simprints.id.data.db.down_sync_info.local.DbDownSyncOperationKey
import com.simprints.id.data.db.down_sync_info.local.DownSyncOperationDao
import com.simprints.id.data.db.down_sync_info.local.fromDbToDomain
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

        require(projectId.isNotBlank()) { "ProjectId required" }
        require(possibleUserId?.isNotBlank() ?: false) { "Login required" }

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
    override suspend fun getDownSyncOperations(syncScope: DownSyncScope): List<DownSyncOperation> {
        val downSyncOpsStoredInDb = fetchDownSyncOperationsFromDb(syncScope)
        return if (downSyncOpsStoredInDb.isEmpty()) {
            createOperations(syncScope).also {
                it.forEach {
                    downSyncOperationDao.insertOrReplaceDownSyncOperation(it.fromDomainToDb())
                }
            }
        } else {
            downSyncOpsStoredInDb.map { it.fromDbToDomain() }
        }
    }


    private fun createOperations(syncScope: DownSyncScope): List<DownSyncOperation> =
        when (syncScope) {
            is ProjectSyncScope -> {
                listOf(DownSyncOperation.buildProjectOperation(syncScope.projectId, syncScope.modes, null))
            }
            is UserSyncScope ->
                listOf(DownSyncOperation.buildUserOperation(syncScope.projectId, syncScope.userId, syncScope.modes, null))
            is ModuleSyncScope ->
                syncScope.modules.map {
                    DownSyncOperation.buildModuleOperation(syncScope.projectId, it, syncScope.modes, null)
                }.toList()
        }

    private suspend fun fetchDownSyncOperationsFromDb(syncScope: DownSyncScope) =
        with(downSyncOperationDao) {
            when (syncScope) {
                is ProjectSyncScope -> {
                    listOf(getDownSyncOperation(DbDownSyncOperationKey(syncScope.projectId, syncScope.modes)))
                }
                is UserSyncScope ->
                    listOf(getDownSyncOperation(DbDownSyncOperationKey(syncScope.projectId, syncScope.modes, userId = syncScope.userId)))
                is ModuleSyncScope ->
                    syncScope.modules.fold(emptyList()) { operations, moduleId ->
                        operations + getDownSyncOperation(DbDownSyncOperationKey(syncScope.projectId, syncScope.modes, moduleId = moduleId))
                    }
            }
        }

    override suspend fun insertOrUpdate(syncScopeOperation: DownSyncOperation) =
        downSyncOperationDao.insertOrReplaceDownSyncOperation(syncScopeOperation.fromDomainToDb())

    override fun deleteAll() {
        runBlocking {
            downSyncOperationDao.deleteAll()
        }
    }

}
