package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.getStatusId
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import io.reactivex.Single
import timber.log.Timber

class CountTaskImpl(private val dbManager: DbManager) : CountTask {

    lateinit var subSyncScope: SubSyncScope

    val projectId
        get() = subSyncScope.projectId
    val userId
        get() = subSyncScope.userId

    val moduleId
        get() = subSyncScope.moduleId

    override fun execute(subSyncScope: SubSyncScope): Single<Int> {
        this.subSyncScope = subSyncScope
        val (projectId, userId, moduleId) = subSyncScope

        Timber.d("Count task executing for module $moduleId")
        return dbManager.calculateNPatientsToDownSync(projectId, userId, moduleId)
    }
}
