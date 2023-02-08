package com.simprints.eventsystem.events_sync.up

import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncScope.ProjectScope
import com.simprints.eventsystem.events_sync.up.domain.getUniqueKey
import com.simprints.eventsystem.events_sync.up.local.DbEventUpSyncOperationStateDao
import com.simprints.eventsystem.events_sync.up.local.DbEventsUpSyncOperationState.Companion.buildFromEventsUpSyncOperationState
import com.simprints.infra.login.LoginManager
import javax.inject.Inject

internal class EventUpSyncScopeRepositoryImpl @Inject constructor(
    val loginManager: LoginManager,
    private val dbEventUpSyncOperationStateDao: DbEventUpSyncOperationStateDao,
) : EventUpSyncScopeRepository {

    override suspend fun getUpSyncScope(): ProjectScope {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()
        val syncScope = ProjectScope(projectId)

        syncScope.operation = refreshState(syncScope.operation)

        return syncScope
    }

    override suspend fun insertOrUpdate(syncScopeOperation: EventUpSyncOperation) {
        dbEventUpSyncOperationStateDao.insertOrUpdate(buildFromEventsUpSyncOperationState(syncScopeOperation))
    }

    private suspend fun refreshState(upOperation: EventUpSyncOperation): EventUpSyncOperation {
        val op = upOperation.copy()
        val state =
            dbEventUpSyncOperationStateDao.load().toList().firstOrNull {
                it.id == op.getUniqueKey()
            }

        return upOperation.copy(lastSyncTime = state?.lastUpdatedTime, lastState = state?.lastState)
    }

    override suspend fun deleteAll() {
        dbEventUpSyncOperationStateDao.deleteAll()
    }
}
