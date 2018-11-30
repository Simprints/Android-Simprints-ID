package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy

import android.util.Log
import androidx.work.Worker
import com.simprints.id.tools.json.JsonHelper
import java.util.*

/**
 * Tris - Worker to execute sync for (p, u, m) using DownCountTask.
 * Invocated by DownSyncWorker
 */
class SubDownSyncWorker : Worker() {

    companion object {
        const val SUBDOWNSYNC_WORKER_TAG = "SUBDOWNSYNC_WORKER_TAG"
    }
    override fun doWork(): Result {
        Log.d("WM", "Running SubDownSyncWorker with ${JsonHelper.toJson(inputData)}")
        Thread.sleep(Random().nextInt(2) * 1000L)
        return Result.SUCCESS
    }
}
