package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy.SubCountWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy.SubDownSyncWorker
import com.simprints.id.services.sync.SyncTaskParameters.Companion.MODULES_ID_FIELD
import com.simprints.id.services.sync.SyncTaskParameters.Companion.MODULE_ID_FIELD
import com.simprints.id.services.sync.SyncTaskParameters.Companion.PROJECT_ID_FIELD
import com.simprints.id.services.sync.SyncTaskParameters.Companion.USER_ID_FIELD
import com.simprints.id.tools.json.JsonHelper
import java.util.concurrent.TimeUnit

/**
 * Fabio - Sync Worker: Worker to chain CountWorker and DownSyncWorker
 * passing SyncParams as Input of the CountWorker.
 */
class SyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val workerManager = WorkManager.getInstance()
    val projectId by lazy {
        inputData.getString(PROJECT_ID_FIELD)
            ?: throw IllegalArgumentException("Project Id required")
    }

    val userId by lazy {
        inputData.getString(USER_ID_FIELD)
    }

    val moduleIds by lazy {
        inputData.getStringArray(MODULES_ID_FIELD)
    }

    companion object {
        const val SYNC_WORKER_REPEAT_INTERVAL = 1L //StopShip: 1h?
        val SYNC_WORKER_REPEAT_UNIT = TimeUnit.HOURS
        const val SYNC_WORKER_TAG = "SYNC_WORKER_TAG"
    }

    override fun doWork(): Result {
        Log.d("WM", "Running SyncWorker with ${JsonHelper.toJson(inputData)}")
        val listOfCountWorkers = getCountWorkers().map { workerManager.beginWith(it) }
        val listOfDownSyncers = getDownSyncWorkers()

        val countersChain = if (listOfCountWorkers.size == 1) {
            listOfCountWorkers[0]
        } else {
            WorkContinuation.combine(*listOfCountWorkers.toTypedArray())
        }

        val syncChain = countersChain.then(listOfDownSyncers)
        syncChain.enqueue()

        Log.d("WM", "All queuing done!")
        return Result.SUCCESS
    }

    //it convert (p, u, listOf(m)) -> listOf(OneTimeWorker<SubDownSyncWorker>(p, u, m))
    private fun getDownSyncWorkers(): List<OneTimeWorkRequest> =
        mutableListOf<OneTimeWorkRequest>().apply {
            moduleIds?.let {
                it.forEach { moduleId ->
                    add(buildDownSyncWorker(projectId, userId, moduleId))
                }
            } ?: add(buildDownSyncWorker(projectId, userId, null))
        }

    //it convert (p, u, listOf(m)) -> listOf(OneTimeWorker<CountSyncWorker>(p, u, m))
    private fun getCountWorkers(): List<OneTimeWorkRequest> =
        mutableListOf<OneTimeWorkRequest>().apply {
            moduleIds?.let {
                it.forEach { moduleId ->
                    add(buildCountWorker(projectId, userId, moduleId))
                }
            } ?: add(buildCountWorker(projectId, userId, null))
        }

    private fun buildDownSyncWorker(projectId: String, userId: String?, moduleId: String?) =
        OneTimeWorkRequestBuilder<SubDownSyncWorker>()
            .addTag(SubDownSyncWorker.SUBDOWNSYNC_WORKER_TAG)
            .setInputData(buildData(projectId, userId, moduleId))
            .build()

    private fun buildCountWorker(projectId: String, userId: String?, moduleId: String?) =
        OneTimeWorkRequestBuilder<SubCountWorker>()
            .addTag(SubCountWorker.SUBCOUNT_WORKER_TAG)
            .setInputData(buildData(projectId, userId, moduleId))
            .build()

    private fun buildData(projectId: String, userId: String?, moduleId: String?): Data =
        workDataOf(
            PROJECT_ID_FIELD to projectId,
            USER_ID_FIELD to userId,
            MODULE_ID_FIELD to moduleId
        )
}
