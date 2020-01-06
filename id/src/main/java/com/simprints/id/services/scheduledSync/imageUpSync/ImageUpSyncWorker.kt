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

    @Inject
    lateinit var imageRepository: ImageRepository

    @Inject
    override lateinit var crashReportManager: CrashReportManager

    override suspend fun doWork(): Result {
        (applicationContext as Application).component.inject(this)

        val success = try {
            uploadAndDeleteIfSuccessful()
        } catch (ex: Exception) {
            crashReportManager.logExceptionOrSafeException(ex)
            false
        }

        return if (success)
            Result.success()
        else
            Result.retry()
    }

    private suspend fun uploadAndDeleteIfSuccessful(): Boolean {
        val uploads = imageRepository.uploadImages()

        if (uploads.isEmpty())
            return true

        var allUploadsSuccessful = true

        uploads.forEach { upload ->
            if (upload.isSuccessful())
                imageRepository.deleteImage(upload.image)
            else
                allUploadsSuccessful = false
        }

        return allUploadsSuccessful
    }

}
