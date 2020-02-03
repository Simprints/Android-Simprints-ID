package com.simprints.id.services.scheduledSync.people.up.controllers

import android.content.Context
import androidx.work.WorkManager

class PeopleUpSyncExecutorImpl(val ctx: Context,
                               private val peopleUpSyncWorkersBuilder: PeopleUpSyncWorkersBuilder) : PeopleUpSyncExecutor {

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    override fun sync() {
        wm.enqueue(peopleUpSyncWorkersBuilder.buildUpSyncWorkerChain(null))
    }
}
