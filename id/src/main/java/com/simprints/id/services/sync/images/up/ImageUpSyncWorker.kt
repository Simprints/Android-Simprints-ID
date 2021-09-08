package com.simprints.id.services.sync.images.up

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.Application
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImageUpSyncWorker(
    context: Context,
    params: WorkerParameters,
) : SimCoroutineWorker(context, params) {

    override val tag: String = ImageUpSyncWorker::class.java.simpleName

    @Inject lateinit var imageRepository: ImageRepository
    @Inject lateinit var dispatcher: DispatcherProvider

    override suspend fun doWork(): Result {
        (applicationContext as Application).component.inject(this@ImageUpSyncWorker)

        return withContext(dispatcher.io()) {
            crashlyticsLog("Start")

            try {
                if (imageRepository.uploadStoredImagesAndDelete()) {
                    success()
                } else {
                    retry()
                }
            } catch (ex: Exception) {
                retry(t = ex)
            }
        }
    }
}
