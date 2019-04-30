package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.*
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.CountWorker.Companion.COUNT_WORKER_SCOPE_INPUT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SubDownSyncWorker.Companion.SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.COUNT_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.SUBDOWNSYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.SYNC_WORKER_CHAIN
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.SYNC_WORKER_TAG
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
        fun getDownSyncWorkerKeyForScope(scope: SubSyncScope) = "${SUBDOWNSYNC_WORKER_TAG}_${scope.uniqueKey}"
        fun getCountWorkerKeyForScope(scope: SyncScope) = "${COUNT_WORKER_TAG}_${scope.uniqueKey}"
    }

    override fun doWork(): Result {
        inject()

        val scope = getScope()
        if (scope.toSubSyncScopes().isNotEmpty()) {
            val countWorker = buildCountWorker(scope)
            val subDownSyncWorkers = scope.toSubSyncScopes().map { this.buildSubDownSyncWorker(it) }

            WorkManager.getInstance()
                .beginUniqueWork(getSyncChainWorkersUniqueNameForSync(scope), ExistingWorkPolicy.KEEP, countWorker)
                .then(buildInputMergerWorker())
                .then(subDownSyncWorkers)
                .enqueue()
        }
        return Result.success()
            .also {
                toastForDebugBuilds(scope, it)
            }
    }

    private fun toastForDebugBuilds(scope: SyncScope, result: Result) {
        if (BuildConfig.DEBUG) {
            applicationContext.runOnUiThread {
                val message = "WM - DownSyncMasterWorker($scope): $result"
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                Timber.d(message)
            }
        }
    }

    private fun getScope(): SyncScope {
        val input = inputData.getString(SYNC_WORKER_SYNC_SCOPE_INPUT)
            ?: throw IllegalArgumentException("input required")
        return syncScopeBuilder.fromJsonToSyncScope(input)
            ?: throw IllegalArgumentException("SyncScope required")
    }

    private fun buildCountWorker(syncScope: SyncScope): OneTimeWorkRequest {
        val data: Data =
            workDataOf(COUNT_WORKER_SCOPE_INPUT to syncScopeBuilder.fromSyncScopeToJson(syncScope))

        return OneTimeWorkRequestBuilder<CountWorker>()
            .setInputData(data)
            .addTag(COUNT_WORKER_TAG)
            .addTag(SYNC_WORKER_TAG)
            .build()
    }

    private fun buildInputMergerWorker(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<InputMergeWorker>()
            .setInputMerger(ArrayCreatingInputMerger::class.java)
            .addTag(SYNC_WORKER_TAG)
            .build()
    }

    private fun buildSubDownSyncWorker(subSyncScope: SubSyncScope): OneTimeWorkRequest {
        val data: Data = workDataOf(
            SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT to syncScopeBuilder.fromSubSyncScopeToJson(subSyncScope)
        )
        return OneTimeWorkRequestBuilder<SubDownSyncWorker>()
            .setInputData(data)
            .addTag(getDownSyncWorkerKeyForScope(subSyncScope))
            .addTag(SUBDOWNSYNC_WORKER_TAG)
            .addTag(SYNC_WORKER_TAG)
            .build()
    }

    private fun inject() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else throw WorkerInjectionFailedException.forWorker<DownSyncMasterWorker>()
    }
}
