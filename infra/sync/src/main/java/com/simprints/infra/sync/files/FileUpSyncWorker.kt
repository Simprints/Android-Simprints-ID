package com.simprints.infra.sync.files

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.fingerprint.infra.imagedistortionconfig.ImageDistortionConfigRepo
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.sync.SyncConstants
import com.simprints.infra.sync.ImageSyncTimestampProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Worker that uploads images and scanner calibration file to the google cloud storage.
 */
@HiltWorker
internal class FileUpSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val imageRepository: ImageRepository,
    private val imageDistortionConfigRepo: ImageDistortionConfigRepo,
    private val authStore: AuthStore,
    private val imageSyncTimestampProvider: ImageSyncTimestampProvider,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {
    override val tag: String = "FileUpSyncWorker"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Started")
        try {
            when {
                !imageDistortionConfigRepo.uploadPendingConfigs() -> retry()
                imageRepository.uploadStoredImagesAndDelete(
                    authStore.signedInProjectId,
                    progressCallback = { currentIndex, max ->
                        setProgress(
                            workDataOf(
                                SyncConstants.PROGRESS_CURRENT to currentIndex,
                                SyncConstants.PROGRESS_MAX to max,
                            )
                        )
                    }
                ) -> success().also {
                    imageSyncTimestampProvider.saveImageSyncCompletionTimestampNow()
                }

                else -> retry()
            }
        } catch (ex: Exception) {
            retry(ex)
        }
    }
}
