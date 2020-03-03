package com.simprints.id.data.db.people_sync.up

import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperation
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState.RUNNING
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncScope
import com.simprints.id.data.db.people_sync.up.domain.fromDbToDomain
import com.simprints.id.data.db.people_sync.up.local.DbUpSyncOperation
import com.simprints.id.data.db.people_sync.up.local.DbUpSyncOperationKey
import com.simprints.id.data.db.people_sync.up.local.PeopleUpSyncOperationLocalDataSource
import com.simprints.id.data.db.people_sync.up.local.fromDbToDomain
import com.simprints.id.data.loginInfo.LoginInfoManager
import java.util.*

class PeopleUpSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                      private val upSyncOperationOperationLocalDataSource: PeopleUpSyncOperationLocalDataSource) :
    PeopleUpSyncScopeRepository {

    override fun getUpSyncScope(): PeopleUpSyncScope {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        return PeopleUpSyncScope(projectId)
    }

    override suspend fun getUpSyncOperations(syncScope: PeopleUpSyncScope): List<PeopleUpSyncOperation> {
        val key = DbUpSyncOperationKey(syncScope.projectId)
        var storedOp = upSyncOperationOperationLocalDataSource.getUpSyncOperation(key)
        if (storedOp == null) {
            storedOp = DbUpSyncOperation(key, syncScope.projectId, RUNNING, Date().time)
            upSyncOperationOperationLocalDataSource.insertOrReplaceUpSyncOperation(storedOp)
        }

        return listOf(storedOp.fromDbToDomain())
    }

    override suspend fun insertOrUpdate(syncScopeOperation: PeopleUpSyncOperation) {
        upSyncOperationOperationLocalDataSource.insertOrReplaceUpSyncOperation(syncScopeOperation.fromDbToDomain())
    }


    override suspend fun deleteAll() {
        upSyncOperationOperationLocalDataSource.deleteAll()
    }
}
