package com.simprints.id.services.scheduledSync.imageUpSync

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.image.repository.ImageRepository
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import javax.inject.Inject

class ImageUpSyncWorker(
    context: Context,
    params: WorkerParameters
) : SimCoroutineWorker(context, params) {

    @Inject lateinit var imageRepository: ImageRepository

    @Inject
    override lateinit var crashReportManager: CrashReportManager

    override suspend fun doWork(): Result {
        (applicationContext as Application).component.inject(this)

        val success = try {
            imageRepository.uploadStoredImagesAndDelete()
        } catch (ex: Exception) {
            crashReportManager.logExceptionOrSafeException(ex)
            false
        }

        return if (success)
            Result.success()
        else
            Result.retry()
    }

}
