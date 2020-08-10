package com.simprints.id.data.db.events_sync.down

import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.id.data.db.events_sync.down.domain.getUniqueKey
import com.simprints.id.data.db.events_sync.down.local.DbEventsDownSyncOperationState.Companion.buildFromEventsDownSyncOperationState
import com.simprints.id.data.db.events_sync.down.local.DbEventsDownSyncOperationStateDao
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.unexpected.MissingArgumentForDownSyncScopeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class EventDownSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                       val preferencesManager: PreferencesManager,
                                       private val downSyncOperationOperationDao: DbEventsDownSyncOperationStateDao) : EventDownSyncScopeRepository {


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
                ProjectScope(projectId, modes)
            GROUP.USER ->
                UserScope(projectId, possibleUserId, modes)
            GROUP.MODULE ->
                ModuleScope(projectId, possibleModuleIds, modes)
        }

        syncScope.operations = syncScope.operations.map { op ->
            refreshState(op).also {
                Timber.d("TEST!!! $it")
            }
        }
        Timber.d("TEST!!! $syncScope")
        return syncScope
    }

    override suspend fun insertOrUpdate(syncScopeOperation: EventDownSyncOperation) {
        withContext(Dispatchers.IO) {
            downSyncOperationOperationDao.insertOrUpdate(buildFromEventsDownSyncOperationState(syncScopeOperation)).also {
                Timber.d("TEST!!! Insert ${buildFromEventsDownSyncOperationState(syncScopeOperation)} id ${syncScopeOperation.getUniqueKey()} for $syncScopeOperation")

            }
        }
    }

    override suspend fun refreshState(syncScopeOperation: EventDownSyncOperation): EventDownSyncOperation {
        val uniqueOpId = syncScopeOperation.getUniqueKey()
        val state =
            downSyncOperationOperationDao.load().toList().firstOrNull {
                it.id == uniqueOpId
            }

        return syncScopeOperation.copy(
            queryEvent = syncScopeOperation.queryEvent.copy(lastEventId = state?.lastEventId),
            lastEventId = state?.lastEventId, 
            lastSyncTime = state?.lastUpdatedTime,
            state = state?.lastState)
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            downSyncOperationOperationDao.deleteAll()
        }
    }
}
