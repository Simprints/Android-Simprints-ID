package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.data.db.DbManager
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import io.reactivex.Single
import timber.log.Timber

class CountTaskImpl(private val dbManager: DbManager) : CountTask {

    override fun execute(subSyncScope: SubSyncScope): Single<Int> {
        val (projectId, userId, moduleId) = subSyncScope

        Timber.d("Count task executing for module $moduleId")
        return dbManager.calculateNPatientsToDownSync(projectId, userId, moduleId)
    }
}
