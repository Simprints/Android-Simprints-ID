package com.simprints.id.data.db.people_sync.down

import com.simprints.id.data.db.people_sync.down.domain.*
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation.Companion.buildModuleSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation.Companion.buildProjectSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation.Companion.buildUserSyncOperation
import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperationDao
import com.simprints.id.data.db.people_sync.down.local.fromDbToDomain
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.unexpected.MissingArgumentForDownSyncScopeException

class PeopleDownSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                        val preferencesManager: PreferencesManager,
                                        private val downSyncOperationOperationDaoDb: DbPeopleDownSyncOperationDao) : PeopleDownSyncScopeRepository {

    override fun getDownSyncScope(): PeopleDownSyncScope {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        val modes: List<Modes> = preferencesManager.modalities.map { it.toMode() }

        val possibleUserId: String? = loginInfoManager.getSignedInUserIdOrEmpty()
        val possibleModuleIds: List<String> = preferencesManager.selectedModules.toList()

        if (projectId.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("ProjectId required")
        }

        if (possibleUserId == null || possibleUserId.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("UserId required")
        }

        return when (preferencesManager.syncGroup) {
            GROUP.GLOBAL -> {
                ProjectSyncScope(projectId, modes)
            }
            GROUP.USER ->
                UserSyncScope(projectId, possibleUserId, modes)
            GROUP.MODULE ->
                ModuleSyncScope(projectId, possibleModuleIds, modes)
        }
    }

    override suspend fun getDownSyncOperations(syncScope: PeopleDownSyncScope): List<PeopleDownSyncOperation> {
        val downSyncOpsStored = createOperations(syncScope)
        return downSyncOpsStored.map { refreshDownSyncOperationFromDb(it) }
    }

    override suspend fun refreshDownSyncOperationFromDb(opToRefresh: PeopleDownSyncOperation): PeopleDownSyncOperation {
        val ops = downSyncOperationOperationDaoDb.getDownSyncOperationsAll()
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
        downSyncOperationOperationDaoDb.insertOrReplaceDownSyncOperation(syncScopeOperation.fromDomainToDb())
    }

    override suspend fun deleteAll() {
        downSyncOperationOperationDaoDb.deleteAll()
    }

}
