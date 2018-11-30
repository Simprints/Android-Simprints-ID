package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy

import android.util.Log
import androidx.work.Worker
import java.util.*

/**
 * Tris - Worker to fetch counter for (p, u, m) using CountTask.
 * Invocated by CountWorker
 */
class SubCountWorker : Worker() {

    companion object {
        const val SUBCOUNT_WORKER_TAG = "SUBCOUNT_WORKER_TAG"
    }
    override fun doWork(): Result {
        Log.d("WM", "Running SubCountWorker with $inputData")
        Thread.sleep(Random().nextInt(2) * 1000L)
        return Result.SUCCESS
    }
}
