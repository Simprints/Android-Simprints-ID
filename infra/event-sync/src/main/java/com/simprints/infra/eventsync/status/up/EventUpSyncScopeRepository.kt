package com.simprints.infra.eventsync.status.up

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope.ProjectScope
import com.simprints.infra.eventsync.status.up.local.DbEventUpSyncOperationStateDao
import com.simprints.infra.eventsync.status.up.local.DbEventsUpSyncOperationState.Companion.buildFromEventsUpSyncOperationState
import javax.inject.Inject

internal class EventUpSyncScopeRepository @Inject constructor(
    val authStore: AuthStore,
    private val dbEventUpSyncOperationStateDao: DbEventUpSyncOperationStateDao,
) {
    suspend fun getUpSyncScope(): ProjectScope {
        val projectId = authStore.signedInProjectId
        val syncScope = ProjectScope(projectId)

        syncScope.operation = refreshState(syncScope.operation)

        return syncScope
    }

    suspend fun insertOrUpdate(syncScopeOperation: EventUpSyncOperation) {
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

    suspend fun deleteAll() {
        dbEventUpSyncOperationStateDao.deleteAll()
    }
}
