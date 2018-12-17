package com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import timber.log.Timber
import javax.inject.Inject
import androidx.work.Result

// TODO: uncomment userId when multitenancy is properly implemented

class PeopleUpSyncPeriodicFlusherWorker(context : Context, params : WorkerParameters)
    : Worker(context, params) {

    @Inject lateinit var peopleUpSyncMaster: PeopleUpSyncMaster

    val projectId by lazy {
        inputData.getString(PROJECT_ID_KEY) ?: throw IllegalArgumentException("Project Id required")
    }

    /*val userId by lazy {
        inputData.getString(USER_ID_KEY) ?: throw IllegalArgumentException("User Id required")
    }*/

    override fun doWork(): Result {
        Timber.d("PeopleUpSyncPeriodicFlusherWorker doWork")
        injectDependencies()
        peopleUpSyncMaster.schedule(projectId/*, userId*/) // TODO: uncomment userId when multitenancy is properly implemented

        return Result.success()
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
