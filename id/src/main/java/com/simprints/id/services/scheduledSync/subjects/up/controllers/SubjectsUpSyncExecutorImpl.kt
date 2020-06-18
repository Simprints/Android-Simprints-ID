package com.simprints.id.services.scheduledSync.subjects.up.controllers

import android.content.Context
import androidx.work.WorkManager

class SubjectsUpSyncExecutorImpl(val ctx: Context,
                                 private val subjectsUpSyncWorkersBuilder: SubjectsUpSyncWorkersBuilder) : SubjectsUpSyncExecutor {

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    override fun sync() {
        wm.enqueue(subjectsUpSyncWorkersBuilder.buildUpSyncWorkerChain(null))
    }
}
