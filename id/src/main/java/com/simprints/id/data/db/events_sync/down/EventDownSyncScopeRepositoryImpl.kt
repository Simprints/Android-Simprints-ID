package com.simprints.id.data.db.events_sync.down

import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.id.data.db.events_sync.down.local.EventDownSyncOperationLocalDataSource
import com.simprints.id.data.db.events_sync.down.local.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.unexpected.MissingArgumentForDownSyncScopeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventDownSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                       val preferencesManager: PreferencesManager,
                                       private val downSyncOperationOperationDao: EventDownSyncOperationLocalDataSource) : EventDownSyncScopeRepository {


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

        syncScope.operations.forEach { op ->
            val state =
                downSyncOperationOperationDao.load().toList().firstOrNull {
                    it.downSyncOp.scopeId == op.scopeId && it.downSyncOp.queryEvent == op.queryEvent }

            state?.downSyncOp?.let { opWithState ->
                op.lastEventId = opWithState.lastEventId
                op.lastSyncTime = opWithState.lastSyncTime
                op.state = opWithState.state
                op.queryEvent = op.queryEvent.copy(lastEventId = opWithState.lastEventId)
            }
        }
        return syncScope
    }

    override suspend fun insertOrUpdate(syncScopeOperation: EventDownSyncOperation) {
        withContext(Dispatchers.IO) {
            downSyncOperationOperationDao.insertOrUpdate(syncScopeOperation.fromDomainToDb())
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            downSyncOperationOperationDao.deleteAll()
        }
    }
}
