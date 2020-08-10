package com.simprints.id.data.db.events_sync.up

import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.SubjectProjectScope
import com.simprints.id.data.db.events_sync.up.domain.getUniqueKey
import com.simprints.id.data.db.events_sync.up.local.DbEventsUpSyncOperationState.Companion.buildFromEventsUpSyncOperationState
import com.simprints.id.data.db.events_sync.up.local.DbEventsUpSyncOperationStateDao
import com.simprints.id.data.loginInfo.LoginInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventUpSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                     private val dbEventsUpSyncOperationStateDao: DbEventsUpSyncOperationStateDao) :
    EventUpSyncScopeRepository {

    override suspend fun getUpSyncScope(): SubjectProjectScope {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty() //STOPSHIP
        val syncScope = SubjectProjectScope(projectId)

        syncScope.operation = refreshState(syncScope.operation)

        return syncScope
    }

    override suspend fun insertOrUpdate(syncScopeOperation: EventUpSyncOperation) {
        withContext(Dispatchers.IO) {
            dbEventsUpSyncOperationStateDao.insertOrUpdate(buildFromEventsUpSyncOperationState(syncScopeOperation))
        }
    }

    private suspend fun refreshState(upOperation: EventUpSyncOperation): EventUpSyncOperation {
        val op = upOperation.copy()
        val state =
            dbEventsUpSyncOperationStateDao.load().toList().firstOrNull {
                it.id == op.getUniqueKey()
            }

        return upOperation.copy(lastSyncTime = state?.lastUpdatedTime, lastState = state?.lastState)
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dbEventsUpSyncOperationStateDao.deleteAll()
        }
    }
}
