package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.work.Worker
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import timber.log.Timber

class PeopleDownsyncWorker: Worker() {


    override fun doWork(): Result {

        val task = PeopleDownsyncTask()

        return try {
            task.execute()
            Result.SUCCESS
        } catch (exception: TransientSyncFailureException) {
            Timber.e(exception)
            Result.RETRY
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            Result.FAILURE
        }
    }
}
