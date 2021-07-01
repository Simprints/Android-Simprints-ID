package com.simprints.id.services.sync.images.up

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.core.analytics.CrashReportManager
import com.simprints.id.Application
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.logging.Simber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImageUpSyncWorker(
    context: Context,
    params: WorkerParameters
) : SimCoroutineWorker(context, params) {

    override val tag: String = ImageUpSyncWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var imageRepository: ImageRepository

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            (applicationContext as Application).component.inject(this@ImageUpSyncWorker)
            crashlyticsLog("Start")

            val success = try {
                imageRepository.uploadStoredImagesAndDelete()
            } catch (ex: Exception) {
                Simber.e(ex)
                false
            }

            if (success)
                success()
            else
                retry()
        }

}
