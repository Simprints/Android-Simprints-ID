package com.simprints.eventsystem.events_sync.down

import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.eventsystem.events_sync.down.domain.getUniqueKey
import com.simprints.eventsystem.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.eventsystem.events_sync.down.local.DbEventsDownSyncOperationState.Companion.buildFromEventsDownSyncOperationState
import com.simprints.eventsystem.exceptions.MissingArgumentForDownSyncScopeException
import com.simprints.infra.login.LoginManager
import javax.inject.Inject

internal class EventDownSyncScopeRepositoryImpl @Inject constructor(
    val loginManager: LoginManager,
    private val downSyncOperationOperationDao: DbEventDownSyncOperationStateDao,
) : EventDownSyncScopeRepository {

    override suspend fun getDownSyncScope(
        modes: List<Modes>,
        selectedModuleIDs: List<String>,
        syncGroup: GROUP
    ): EventDownSyncScope {
        val projectId = getProjectId()
        val possibleUserId = getUserId()

        val syncScope = when (syncGroup) {
            GROUP.GLOBAL ->
                SubjectProjectScope(projectId, modes)
            GROUP.USER ->
                SubjectUserScope(projectId, possibleUserId, modes)
            GROUP.MODULE ->
                SubjectModuleScope(projectId, selectedModuleIDs, modes)
        }

        syncScope.operations = syncScope.operations.map { op ->
            refreshState(op)
        }
        return syncScope
    }

    private fun getProjectId(): String {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()
        if (projectId.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("ProjectId required")
        }
        return projectId
    }

    private fun getUserId(): String {
        val possibleUserId: String = loginManager.getSignedInUserIdOrEmpty()
        if (possibleUserId.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("UserId required")
        }
        return possibleUserId
    }

    override suspend fun insertOrUpdate(syncScopeOperation: EventDownSyncOperation) {
        downSyncOperationOperationDao.insertOrUpdate(buildFromEventsDownSyncOperationState(syncScopeOperation))
    }

    override suspend fun refreshState(syncScopeOperation: EventDownSyncOperation): EventDownSyncOperation {
        val uniqueOpId = syncScopeOperation.getUniqueKey()
        val state =
            downSyncOperationOperationDao.load().firstOrNull {
                it.id == uniqueOpId
            }

        return syncScopeOperation.copy(
            queryEvent = syncScopeOperation.queryEvent.copy(lastEventId = state?.lastEventId),
            lastEventId = state?.lastEventId,
            lastSyncTime = state?.lastUpdatedTime,
            state = state?.lastState
        )
    }

    override suspend fun deleteOperations(moduleIds: List<String>, modes: List<Modes>) {
        val scope = SubjectModuleScope(getProjectId(), moduleIds, modes)
        scope.operations.forEach {
            downSyncOperationOperationDao.delete(it.getUniqueKey())
        }
    }

    override suspend fun deleteAll() {
        downSyncOperationOperationDao.deleteAll()
    }
}
