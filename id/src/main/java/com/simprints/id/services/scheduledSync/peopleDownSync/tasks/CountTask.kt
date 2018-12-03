package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.getStatusId
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class CountTask(component: AppComponent, subSyncScope: SubSyncScope) {

    val projectId = subSyncScope.projectId
    val userId = subSyncScope.userId
    val moduleId = subSyncScope.moduleId

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var newSyncStatusDatabase: SyncStatusDatabase

    init {
        component.inject(this)
    }

    fun execute(): Single<Int> {
        Timber.d("Count task executing for module $moduleId")
        return dbManager
            .calculateNPatientsToDownSync(projectId, userId, moduleId)
            .insertNewCountForDownSyncStatus()
    }

    private fun Single<out Int>.insertNewCountForDownSyncStatus() =
        map {

            val downSyncStatus = newSyncStatusDatabase.downSyncDao.getDownSyncStatusForId(getDownSyncId())
                ?: DownSyncStatus(projectId = projectId, userId = userId, moduleId = moduleId)
            downSyncStatus.totalToDownload = it
            newSyncStatusDatabase.downSyncDao.insertOrReplaceDownSyncStatus(downSyncStatus)

            it
        }

    private fun getDownSyncId() = newSyncStatusDatabase.downSyncDao.getStatusId(projectId, userId, moduleId)
}
