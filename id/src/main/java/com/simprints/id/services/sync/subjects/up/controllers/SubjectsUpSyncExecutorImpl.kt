package com.simprints.id.services.sync.subjects.up.controllers

import android.content.Context
import androidx.work.WorkManager
import com.simprints.id.services.sync.events.up.SubjectsUpSyncWorkersBuilder

class SubjectsUpSyncExecutorImpl(val ctx: Context,
                                 private val subjectsUpSyncWorkersBuilder: SubjectsUpSyncWorkersBuilder) : SubjectsUpSyncExecutor {

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    override fun sync() {
        wm.enqueue(subjectsUpSyncWorkersBuilder.buildUpSyncWorkerChain(null))
    }
}
