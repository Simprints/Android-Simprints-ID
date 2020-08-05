package com.simprints.id.data.db.events_sync.up

import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.SubjectProjectScope
import com.simprints.id.data.db.events_sync.up.local.EventsUpSyncOperationLocalDataSource
import com.simprints.id.data.db.events_sync.up.local.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager

class SubjectsUpSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                        private val eventsUpSyncOperationLocalDataSource: EventsUpSyncOperationLocalDataSource) :
    SubjectsUpSyncScopeRepository {

    override suspend fun getUpSyncScope(): SubjectProjectScope {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        return SubjectProjectScope(projectId)
    }

    override suspend fun insertOrUpdate(syncScopeOperation: EventUpSyncOperation) {
        eventsUpSyncOperationLocalDataSource.insertOrUpdate(syncScopeOperation.fromDomainToDb())
    }


    override suspend fun deleteAll() {
        eventsUpSyncOperationLocalDataSource.deleteAll()
    }
}
