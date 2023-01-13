package com.simprints.id.services.sync.images

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.simprints.id.services.sync.images.up.ImageUpSyncWorker
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ImageUpSyncWorkerTest {

    companion object {
        private const val PROJECT_ID = "projectId"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val imageRepository = mockk<ImageRepository>()
    private val loginManager = mockk<LoginManager> {
        every { getSignedInProjectIdOrEmpty() } returns PROJECT_ID
    }

    private val imageUpSyncWorker = ImageUpSyncWorker(
        mockk(relaxed = true),
        mockk(relaxed = true),
        imageRepository,
        loginManager,
        testCoroutineRule.testCoroutineDispatcher,
    )


    @Test
    fun whenAllUploadsAreSuccessful_shouldReturnSuccess() = runTest {
        coEvery { imageRepository.uploadStoredImagesAndDelete(PROJECT_ID) } returns true

        val result = imageUpSyncWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun whenNotAllUploadsAreSuccessful_shouldReturnRetry() = runTest {
        coEvery { imageRepository.uploadStoredImagesAndDelete(PROJECT_ID) } returns false

        val result = imageUpSyncWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }
}
