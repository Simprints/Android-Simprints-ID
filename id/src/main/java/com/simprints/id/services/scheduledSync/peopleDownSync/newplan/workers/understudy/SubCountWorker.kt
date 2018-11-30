package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.tools.json.JsonHelper
import java.util.*

/**
 * Tris - Worker to fetch counter for (p, u, m) using CountTask.
 * Invocated by CountWorker
 */
class SubCountWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val SUBCOUNT_WORKER_TAG = "SUBCOUNT_WORKER_TAG"
    }

    override fun doWork(): Result {
        Thread.sleep(Random().nextInt(2) * 1000L)

        return if (Random().nextBoolean()) {
            val total = Random().nextInt(100)
            Log.d("WM", "Succeed SubCountWorker with ${JsonHelper.toJson(inputData)}. Fetched: $total")

            //val input = inputData.keyValueMap.toMutableMap()
            //input["total"] = total
            outputData = Data.Builder().putAll(inputData).put("total", total).build()
            Result.SUCCESS
        } else {
            Log.d("WM", "Failed SubCountWorker with ${JsonHelper.toJson(inputData)}")
            Result.SUCCESS
        }
    }
}
