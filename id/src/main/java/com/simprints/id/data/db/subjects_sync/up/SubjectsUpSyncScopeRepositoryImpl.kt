package com.simprints.id.data.db.subjects_sync.up

import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncOperation
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncOperationResult.UpSyncState.RUNNING
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncScope
import com.simprints.id.data.db.subjects_sync.up.domain.fromDbToDomain
import com.simprints.id.data.db.subjects_sync.up.local.DbUpSyncOperation
import com.simprints.id.data.db.subjects_sync.up.local.DbUpSyncOperationKey
import com.simprints.id.data.db.subjects_sync.up.local.SubjectsUpSyncOperationLocalDataSource
import com.simprints.id.data.db.subjects_sync.up.local.fromDbToDomain
import com.simprints.id.data.loginInfo.LoginInfoManager
import java.util.*

class SubjectsUpSyncScopeRepositoryImpl(val loginInfoManager: LoginInfoManager,
                                        private val upSyncOperationOperationLocalDataSource: SubjectsUpSyncOperationLocalDataSource) :
    SubjectsUpSyncScopeRepository {

    override fun getUpSyncScope(): SubjectsUpSyncScope {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        return SubjectsUpSyncScope(projectId)
    }

    override suspend fun getUpSyncOperations(syncScope: SubjectsUpSyncScope): List<SubjectsUpSyncOperation> {
        val key = DbUpSyncOperationKey(syncScope.projectId)
        var storedOp = upSyncOperationOperationLocalDataSource.getUpSyncOperation(key)
        if (storedOp == null) {
            storedOp = DbUpSyncOperation(key, syncScope.projectId, RUNNING, Date().time)
            upSyncOperationOperationLocalDataSource.insertOrReplaceUpSyncOperation(storedOp)
        }

        return listOf(storedOp.fromDbToDomain())
    }

    override suspend fun insertOrUpdate(syncScopeOperation: SubjectsUpSyncOperation) {
        upSyncOperationOperationLocalDataSource.insertOrReplaceUpSyncOperation(syncScopeOperation.fromDbToDomain())
    }


    override suspend fun deleteAll() {
        upSyncOperationOperationLocalDataSource.deleteAll()
    }
}
