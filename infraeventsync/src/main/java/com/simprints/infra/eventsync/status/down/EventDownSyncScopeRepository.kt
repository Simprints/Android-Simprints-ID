package com.simprints.infra.eventsync.status.down

import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.infra.eventsync.exceptions.MissingArgumentForDownSyncScopeException
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.*
import com.simprints.infra.eventsync.status.down.local.DbEventDownSyncOperationStateDao
import com.simprints.infra.eventsync.status.down.local.DbEventsDownSyncOperationState.Companion.buildFromEventsDownSyncOperationState
import com.simprints.infra.login.LoginManager
import javax.inject.Inject

internal class EventDownSyncScopeRepository @Inject constructor(
    private val loginManager: LoginManager,
    private val downSyncOperationOperationDao: DbEventDownSyncOperationStateDao,
) {

    suspend fun getDownSyncScope(
        modes: List<Modes>,
        selectedModuleIDs: List<String>,
        syncGroup: GROUP
    ): EventDownSyncScope {
        val projectId = getProjectId()
        val possibleUserId = getUserId()

        val syncScope = when (syncGroup) {
            GROUP.GLOBAL -> SubjectProjectScope(projectId, modes)
            GROUP.USER -> SubjectUserScope(projectId, possibleUserId, modes)
            GROUP.MODULE -> SubjectModuleScope(projectId, selectedModuleIDs, modes)
        }

        syncScope.operations = syncScope.operations.map { op -> refreshState(op) }
        return syncScope
    }

    private fun getProjectId(): String {
        val projectId = loginManager.signedInProjectId
        if (projectId.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("ProjectId required")
        }
        return projectId
    }

    private fun getUserId(): String {
        val possibleUserId: String = loginManager.signedInUserId
        if (possibleUserId.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("UserId required")
        }
        return possibleUserId
    }

    suspend fun insertOrUpdate(syncScopeOperation: EventDownSyncOperation) {
        downSyncOperationOperationDao.insertOrUpdate(buildFromEventsDownSyncOperationState(syncScopeOperation))
    }

    suspend fun refreshState(syncScopeOperation: EventDownSyncOperation): EventDownSyncOperation {
        val uniqueOpId = syncScopeOperation.getUniqueKey()
        val state = downSyncOperationOperationDao.load().firstOrNull { it.id == uniqueOpId }

        return syncScopeOperation.copy(
            queryEvent = syncScopeOperation.queryEvent.copy(lastEventId = state?.lastEventId),
            lastEventId = state?.lastEventId,
            lastSyncTime = state?.lastUpdatedTime,
            state = state?.lastState
        )
    }

    suspend fun deleteOperations(moduleIds: List<String>, modes: List<Modes>) {
        val scope = SubjectModuleScope(getProjectId(), moduleIds, modes)
        scope.operations.forEach {
            downSyncOperationOperationDao.delete(it.getUniqueKey())
        }
    }

    suspend fun deleteAll() {
        downSyncOperationOperationDao.deleteAll()
    }
}
