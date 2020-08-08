package com.simprints.id.data.db.events_sync.up

import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.SubjectProjectScope
import com.simprints.id.data.db.events_sync.up.local.EventsUpSyncOperationLocalDataSource
import com.simprints.id.data.db.events_sync.up.local.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventUpSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                     private val eventsUpSyncOperationLocalDataSource: EventsUpSyncOperationLocalDataSource) :
    EventUpSyncScopeRepository {

    override suspend fun getUpSyncScope(): SubjectProjectScope {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty() //STOPSHIP
        return SubjectProjectScope(projectId)
    }

    override suspend fun insertOrUpdate(syncScopeOperation: EventUpSyncOperation) {
        withContext(Dispatchers.IO) {
            eventsUpSyncOperationLocalDataSource.insertOrUpdate(syncScopeOperation.fromDomainToDb())
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            eventsUpSyncOperationLocalDataSource.deleteAll()
        }
    }
}
