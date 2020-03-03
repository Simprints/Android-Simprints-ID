package com.simprints.id.services.scheduledSync.imageUpSync

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.core.images.repository.ImageRepository
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImageUpSyncWorker(
    context: Context,
    params: WorkerParameters
) : SimCoroutineWorker(context, params) {

    override val tag: String = ImageUpSyncWorker::class.java.simpleName

    @Inject lateinit var imageRepository: ImageRepository
    @Inject override lateinit var crashReportManager: CrashReportManager

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            (applicationContext as Application).component.inject(this@ImageUpSyncWorker)
            crashlyticsLog("Start")

            val success = try {
                imageRepository.uploadStoredImagesAndDelete()
            } catch (ex: Exception) {
                crashReportManager.logExceptionOrSafeException(ex)
                false
            }

            if (success)
                success()
            else
                retry()
        }

}
