package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.*
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SUBCOUNT_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SUBDOWNSYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SYNC_WORKER_CHAIN
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SubCountWorker.Companion.SUBCOUNT_WORKER_SUB_SCOPE_INPUT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SubDownSyncWorker.Companion.SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DownSyncMasterWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @Inject lateinit var syncScopeBuilder: SyncScopesBuilder

    companion object {
        const val SYNC_WORKER_REPEAT_INTERVAL = 6L
        val SYNC_WORKER_REPEAT_UNIT = TimeUnit.HOURS

        const val SYNC_WORKER_SYNC_SCOPE_INPUT = "SYNC_WORKER_SYNC_SCOPE_INPUT"

        fun getSyncChainWorkersUniqueNameForSync(scope: SyncScope) = "${SYNC_WORKER_CHAIN}_${scope.uniqueKey}"
        fun getDownSyncWorkerKeyForScope(scope: SubSyncScope) = "${SUBCOUNT_WORKER_TAG}_${scope.uniqueKey}"
        fun getCountWorkerKeyForScope(scope: SubSyncScope) = "${SUBCOUNT_WORKER_TAG}_${scope.uniqueKey}"
    }

    override fun doWork(): Result {
        inject()

        val scope = getScope()
        val subCountWorkers = buildChainOfSubCountWorker(scope)
        val subDownSyncWorkers = scope.toSubSyncScopes().map { this.buildSubDownSyncWorker(it) }

        WorkManager.getInstance()
            .beginUniqueWork(getSyncChainWorkersUniqueNameForSync(scope), ExistingWorkPolicy.KEEP, subCountWorkers)
            .then(buildInputMergerWorker())
            .then(subDownSyncWorkers)
            .enqueue()

        return Result.SUCCESS.also {
            if (BuildConfig.DEBUG) {
                applicationContext.runOnUiThread {
                    val message = "WM - DownSyncMasterWorker($scope): $it"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    Timber.d(message)
                }
            }
        }
    }

    private fun getScope(): SyncScope {
        val input = inputData.getString(SYNC_WORKER_SYNC_SCOPE_INPUT)
            ?: throw IllegalArgumentException("input required")
        return syncScopeBuilder.fromJsonToSyncScope(input)
            ?: throw IllegalArgumentException("SyncScope required")
    }

    private fun buildSubDownSyncWorker(subSyncScope: SubSyncScope): OneTimeWorkRequest {
        val data: Data = workDataOf(SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT to syncScopeBuilder.fromSubSyncScopeToJson(subSyncScope))

        return OneTimeWorkRequestBuilder<SubDownSyncWorker>()
            .setInputData(data)
            .addTag(getDownSyncWorkerKeyForScope(subSyncScope))
            .addTag(SUBDOWNSYNC_WORKER_TAG)
            .addTag(SYNC_WORKER_TAG)
            .build()
    }

    private fun buildChainOfSubCountWorker(scope: SyncScope) = scope.toSubSyncScopes().map { this.buildSubCountWorker(it) }

    private fun buildSubCountWorker(subSyncScope: SubSyncScope): OneTimeWorkRequest {
        val data: Data = workDataOf(SUBCOUNT_WORKER_SUB_SCOPE_INPUT to syncScopeBuilder.fromSubSyncScopeToJson(subSyncScope))

        return OneTimeWorkRequestBuilder<SubCountWorker>()
            .setInputData(data)
            .addTag(getCountWorkerKeyForScope(subSyncScope))
            .addTag(SUBCOUNT_WORKER_TAG)
            .addTag(SYNC_WORKER_TAG)
            .build()
    }

    private fun buildInputMergerWorker(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<InputMergeWorker>()
            .setInputMerger(ArrayCreatingInputMerger::class.java)
            .build()
    }

    private fun inject() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else throw SimprintsError("Cannot get app component in Worker")
    }
}
