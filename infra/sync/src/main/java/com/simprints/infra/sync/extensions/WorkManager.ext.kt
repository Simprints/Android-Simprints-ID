package com.simprints.infra.sync.extensions

import androidx.work.*
import com.simprints.infra.sync.SyncConstants


internal fun defaultWorkerConstraints() = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

internal inline fun <reified T : ListenableWorker> WorkManager.schedulePeriodicWorker(
    workName: String,
    repeatInterval: Long,
    existingWorkPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
    constraints: Constraints = defaultWorkerConstraints(),
    tags: List<String> = emptyList(),
    inputData: Data? = null,
) = enqueueUniquePeriodicWork(
    workName,
    existingWorkPolicy,
    PeriodicWorkRequestBuilder<T>(repeatInterval, SyncConstants.SYNC_REPEAT_UNIT)
        .setConstraints(constraints)
        .let { if (inputData != null) it.setInputData(inputData) else it }
        .let { tags.fold(it) { builder, tag -> builder.addTag(tag) } }
        .build()
)

internal inline fun <reified T : ListenableWorker> WorkManager.startWorker(
    workName: String,
    constraints: Constraints = defaultWorkerConstraints(),
    tags: List<String> = emptyList(),
    inputData: Data? = null,
) = this.enqueueUniqueWork(
    workName,
    ExistingWorkPolicy.KEEP,
    OneTimeWorkRequestBuilder<T>()
        .setConstraints(constraints)
        .let { if (inputData != null) it.setInputData(inputData) else it }
        .let { tags.fold(it) { builder, tag -> builder.addTag(tag) } }
        .build()
)

internal fun WorkManager.cancelWorkers(vararg workNames: String) {
    workNames.forEach(this::cancelUniqueWork)
}
