package com.simprints.id.services.sync.images

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.simprints.id.services.sync.images.up.ImageUpSyncWorker
import com.simprints.infra.images.ImageRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ImageUpSyncWorkerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val imageRepository = mockk<ImageRepository>()

    private val imageUpSyncWorker = ImageUpSyncWorker(
        mockk(relaxed = true),
        mockk(relaxed = true),
        imageRepository,
        testCoroutineRule.testCoroutineDispatcher,
    )


    @Test
    fun whenAllUploadsAreSuccessful_shouldReturnSuccess() = runTest {
        coEvery { imageRepository.uploadStoredImagesAndDelete() } returns true

        val result = imageUpSyncWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun whenNotAllUploadsAreSuccessful_shouldReturnRetry() = runTest {
        coEvery { imageRepository.uploadStoredImagesAndDelete() } returns false

        val result = imageUpSyncWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }
}
