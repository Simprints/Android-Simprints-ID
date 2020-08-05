package com.simprints.id.data.db.events_sync.down

import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.id.data.db.events_sync.down.local.EventDownSyncOperationLocalDataSource
import com.simprints.id.data.db.events_sync.down.local.fromDbToDomain
import com.simprints.id.data.db.events_sync.down.local.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.unexpected.MissingArgumentForDownSyncScopeException

class EventDownSyncScopeRepoImpl(val loginInfoManager: LoginInfoManager,
                                 val preferencesManager: PreferencesManager,
                                 private val downSyncOperationOperationDao: EventDownSyncOperationLocalDataSource) : EventDownSyncScopeRepo {


    override suspend fun getDownSyncScope(): EventDownSyncScope {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        val modes: List<Modes> = preferencesManager.modalities.map { it.toMode() }

        val possibleUserId: String? = loginInfoManager.getSignedInUserIdOrEmpty()
        val possibleModuleIds: List<String> = preferencesManager.selectedModules.toList()

        if (projectId.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("ProjectId required")
        }

        if (possibleUserId.isNullOrBlank()) {
            throw MissingArgumentForDownSyncScopeException("UserId required")
        }

        val syncScope = when (preferencesManager.syncGroup) {
            GROUP.GLOBAL ->
                SubjectProjectScope(projectId, modes)
            GROUP.USER ->
                SubjectUserScope(projectId, possibleUserId, modes)
            GROUP.MODULE ->
                SubjectModuleScope(projectId, possibleModuleIds, modes)
        }

        syncScope.operations.addAll(fetchOperationFor(syncScope))
        return syncScope
    }

    private suspend fun fetchOperationFor(syncScope: EventDownSyncScope): List<EventDownSyncOperation> =
        downSyncOperationOperationDao.load().filter {
            it.downSyncOp.scopeId == syncScope.id
        }.map { it.fromDbToDomain() }

    override suspend fun insertOrUpdate(syncScopeOperation: EventDownSyncOperation) {
        downSyncOperationOperationDao.insertOrUpdate(syncScopeOperation.fromDomainToDb())
    }

    override suspend fun deleteAll() {
        downSyncOperationOperationDao.deleteAll()
    }
}
