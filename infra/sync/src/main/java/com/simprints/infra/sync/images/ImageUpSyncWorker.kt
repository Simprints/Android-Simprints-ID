package com.simprints.infra.sync.images

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.images.ImageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
internal class ImageUpSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val imageRepository: ImageRepository,
    private val authStore: AuthStore,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {

    override val tag: String = ImageUpSyncWorker::class.java.simpleName

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Image upload start")

        try {
            if (imageRepository.uploadStoredImagesAndDelete(authStore.signedInProjectId)) {
                success()
            } else {
                retry()
            }
        } catch (ex: Exception) {
            retry(t = ex)
        }
    }
}
