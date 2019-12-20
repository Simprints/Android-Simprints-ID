package com.simprints.id.data.db.people_sync.down

import com.simprints.id.data.db.people_sync.down.domain.*
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation.Companion.buildModuleSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation.Companion.buildProjectSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation.Companion.buildUserSyncOperation
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncDao
import com.simprints.id.data.db.people_sync.down.local.fromDbToDomain
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.modality.toMode

class PeopleDownSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                        val preferencesManager: PreferencesManager,
                                        private val downSyncOperationDao: PeopleDownSyncDao) : PeopleDownSyncScopeRepository {

    override fun getDownSyncScope(): PeopleDownSyncScope {
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

    override suspend fun getDownSyncOperations(syncScope: PeopleDownSyncScope): List<PeopleDownSyncOperation> {
        val downSyncOpsStored = createOperations(syncScope)
        return downSyncOpsStored.map { refreshDownSyncOperationFromDb(it) }
    }

    override suspend fun refreshDownSyncOperationFromDb(opToRefresh: PeopleDownSyncOperation): PeopleDownSyncOperation {
        val ops = downSyncOperationDao.getDownSyncOperationsAll()
        return ops.firstOrNull {
            it.projectId == opToRefresh.projectId &&
                it.userId == opToRefresh.userId &&
                it.moduleId == opToRefresh.moduleId &&
                it.modes.toTypedArray() contentEquals opToRefresh.modes.toTypedArray()
        }?.fromDbToDomain() ?: opToRefresh
    }

    private fun createOperations(syncScope: PeopleDownSyncScope): List<PeopleDownSyncOperation> =
        when (syncScope) {
            is ProjectSyncScope -> {
                listOf(buildProjectSyncOperation(syncScope.projectId, syncScope.modes, null))
            }
            is UserSyncScope ->
                listOf(buildUserSyncOperation(syncScope.projectId, syncScope.userId, syncScope.modes, null))
            is ModuleSyncScope ->
                syncScope.modules.map {
                    buildModuleSyncOperation(syncScope.projectId, it, syncScope.modes, null)
                }.toList()
        }

    override suspend fun insertOrUpdate(syncScopeOperation: PeopleDownSyncOperation) {
        downSyncOperationDao.insertOrReplaceDownSyncOperation(syncScopeOperation.fromDomainToDb())
    }

    override suspend fun deleteAll() {
        downSyncOperationDao.deleteAll()
    }

}
