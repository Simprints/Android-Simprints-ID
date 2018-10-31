package com.simprints.id.services.scheduledSync.peopleDownSync.periodicDownSyncCount

import androidx.work.Worker
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncCountTask

class PeriodicDownSyncCountWorker: Worker() {

    override fun doWork(): Result {
        //TODO: refer design spec
        val task = PeopleDownSyncCountTask()
        task.execute()
        return Result.SUCCESS
    }
}
