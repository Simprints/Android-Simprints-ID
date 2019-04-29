package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.data.db.DbManager
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Single

class CountTaskImpl(private val dbManager: DbManager) : CountTask {

    override fun execute(syncScope: SyncScope): Single<Int> {
        return dbManager.calculateNPatientsToDownSync(syncScope)
    }
}
