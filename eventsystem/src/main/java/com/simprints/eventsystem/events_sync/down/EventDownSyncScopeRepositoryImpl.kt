package com.simprints.eventsystem.events_sync.down

import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.eventsystem.events_sync.down.domain.getUniqueKey
import com.simprints.eventsystem.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.eventsystem.events_sync.down.local.DbEventsDownSyncOperationState.Companion.buildFromEventsDownSyncOperationState
import com.simprints.eventsystem.exceptions.MissingArgumentForDownSyncScopeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventDownSyncScopeRepositoryImpl(
    val loginInfoManager: LoginInfoManager,
    val preferencesManager: PreferencesManager,
    private val downSyncOperationOperationDao: DbEventDownSyncOperationStateDao
) : EventDownSyncScopeRepository {


    override suspend fun getDownSyncScope(
        modes: List<Modes>,
        possibleModuleIds: List<String>,
        syncGroup: GROUP): EventDownSyncScope {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()

        val possibleUserId: String? = loginInfoManager.getSignedInUserIdOrEmpty()

        if (projectId.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("ProjectId required")
        }

        if (possibleUserId.isNullOrBlank()) {
            throw MissingArgumentForDownSyncScopeException("UserId required")
        }

        val syncScope = when (syncGroup) {
            GROUP.GLOBAL ->
                SubjectProjectScope(projectId, modes)
            GROUP.USER ->
                SubjectUserScope(projectId, possibleUserId, modes)
            GROUP.MODULE ->
                SubjectModuleScope(projectId, possibleModuleIds, modes)
        }

        syncScope.operations = syncScope.operations.map { op ->
            refreshState(op)
        }
        return syncScope
    }

    override suspend fun insertOrUpdate(syncScopeOperation: EventDownSyncOperation) {
        withContext(Dispatchers.IO) {
            downSyncOperationOperationDao.insertOrUpdate(
                buildFromEventsDownSyncOperationState(
                    syncScopeOperation
                )
            )
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
            state = state?.lastState
        )
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            downSyncOperationOperationDao.deleteAll()
        }
    }
}
