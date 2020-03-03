package com.simprints.id.services.scheduledSync.sessionSync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

class SessionEventsMasterWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var crashReportManager: CrashReportManager

    override suspend fun doWork(): Result {
        Timber.d("SessionEventsMasterWorker doWork()")
        injectDependencies()

        return try {
            runBlocking { sessionRepository.startUploadingSessions() }
            Result.success()
        } catch (e: NoSessionsFoundException) {
            Timber.d("No sessions found")
            Result.success()
        } catch (throwable: Throwable) {
            Timber.d("Sessions upload failed")
            Timber.e(throwable)
            crashReportManager.logExceptionOrSafeException(throwable)
            Result.failure()
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else {
            throw WorkerInjectionFailedException.forWorker<SessionEventsMasterWorker>()
        }
    }
}
