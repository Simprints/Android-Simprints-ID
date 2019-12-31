package com.simprints.id.services.scheduledSync.imageUpSync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.db.image.remote.UploadResult
import com.simprints.id.data.db.image.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImageUpSyncWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params), CoroutineScope {

    private val job = Job()

    override val coroutineContext = job + Dispatchers.IO

    @Inject
    lateinit var imageRepository: ImageRepository

    override fun doWork(): Result {
        (applicationContext as Application).component.inject(this)
        var uploadResults = emptyList<UploadResult>()

        launch {
            uploadResults = uploadAndDeleteIfSuccessful()
        }

        return if (uploadResults.allSuccessful())
            Result.success()
        else
            Result.retry() // TODO: implement retry policy
    }

    private suspend fun uploadAndDeleteIfSuccessful(): List<UploadResult> {
        return imageRepository.uploadImages().apply {
            forEach { upload ->
                if (upload.isSuccessful())
                    imageRepository.deleteImage(upload.image)
            }
        }
    }

    private fun List<UploadResult>.allSuccessful(): Boolean {
        return all { it.isSuccessful() }
    }

}
