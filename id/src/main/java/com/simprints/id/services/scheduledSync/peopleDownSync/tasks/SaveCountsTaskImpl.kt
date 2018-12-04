package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.getStatusId
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope


class SaveCountsTaskImpl(syncStatusDatabase: SyncStatusDatabase) : SaveCountsTask {

    private val dao = syncStatusDatabase.downSyncDao

    override fun execute(countsForSubScopes: Map<SubSyncScope, Int>) {
        countsForSubScopes.forEach {
            val subScope = it.key
            val downSyncStatus = dao.getDownSyncStatusForId(dao.getStatusId(subScope))
                ?: DownSyncStatus(projectId = subScope.projectId, userId = subScope.userId, moduleId = subScope.moduleId)
            downSyncStatus.totalToDownload = it.value
            dao.insertOrReplaceDownSyncStatus(downSyncStatus)
        }
    }
}
