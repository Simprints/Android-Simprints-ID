package com.simprints.id.services.scheduledSync.sessionSync

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import com.simprints.id.services.scheduledSync.subjects.common.SimCoroutineWorker
import timber.log.Timber
import javax.inject.Inject

class UpSessionEventsWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    override val tag: String = UpSessionEventsWorker::class.java.simpleName
    @Inject lateinit var eventRepository: EventRepository
    @Inject override lateinit var crashReportManager: CrashReportManager

    override suspend fun doWork(): Result {
        Timber.d("UpSessionEventsWorker doWork()")
        injectDependencies()

        return try {
            eventRepository.uploadEvents()
            success()
        } catch (ex: NoSessionsFoundException) {
            Timber.d("No sessions found")
            success()
        } catch (throwable: Throwable) {
            Timber.d("Sessions upload failed")
            Timber.e(throwable)
            crashReportManager.logExceptionOrSafeException(throwable)
            retry()
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else {
            throw WorkerInjectionFailedException.forWorker<UpSessionEventsWorker>()
        }
    }
}
