package com.simprints.id.services.scheduledSync.peopleDownSync.oneTimeDownSyncCount

import androidx.work.Worker
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncCountTask

class OneTimeDownSyncCountWorker: Worker() {

    override fun doWork(): Result {
        //TODO: refer design spec
        val task = PeopleDownSyncCountTask()
        task.execute()
        return Result.SUCCESS
    }
}
