package com.simprints.id.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.simprints.infra.config.worker.ConfigurationWorker
import javax.inject.Inject

// TODO remove when using hilt
class CustomWorkerFactory @Inject constructor(
    private val configurationWorkerFactory: ConfigurationWorker.Factory,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? =
        when (workerClassName) {
            ConfigurationWorker::class.java.name ->
                configurationWorkerFactory.create(appContext, workerParameters)
            else -> null
        }
}
