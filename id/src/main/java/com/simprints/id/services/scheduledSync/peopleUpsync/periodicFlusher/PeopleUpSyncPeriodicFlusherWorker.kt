package com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import timber.log.Timber
import javax.inject.Inject

class PeopleUpSyncPeriodicFlusherWorker : Worker() {

    @Inject lateinit var peopleUpSyncMaster: PeopleUpSyncMaster

    val projectId by lazy {
        inputData.getString(PROJECT_ID_KEY) ?: throw IllegalArgumentException("Project Id required")
    }

    val userId by lazy {
        inputData.getString(USER_ID_KEY) ?: throw IllegalArgumentException("User Id required")
    }

    override fun doWork(): Result {
        Timber.d("PeopleUpSyncPeriodicFlusherWorker doWork")
        injectDependencies()
        peopleUpSyncMaster.schedule(projectId, userId)
        return Result.SUCCESS
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }

    companion object {
        const val PROJECT_ID_KEY = "projectId"
        const val USER_ID_KEY = "userId"
    }
}
