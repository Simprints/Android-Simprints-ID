package com.simprints.infra.sync.files

import androidx.work.ListenableWorker.Result
import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.imagedistortionconfig.ImageDistortionConfigRepo
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.images.ImageRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FileUpSyncWorkerTest {
    companion object {
        private const val PROJECT_ID = "projectId"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var imageRepository: ImageRepository

    @MockK
    private lateinit var imageDistortionConfigRepo: ImageDistortionConfigRepo

    @MockK
    private lateinit var authStore: AuthStore

    private lateinit var fileUpSyncWorker: FileUpSyncWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { authStore.signedInProjectId } returns PROJECT_ID

        fileUpSyncWorker = FileUpSyncWorker(
            mockk(relaxed = true),
            mockk(relaxed = true),
            imageRepository,
            imageDistortionConfigRepo,
            authStore,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `doWork returns retry when uploadPendingConfigs fails`() = runBlocking {
        // Given
        coEvery { imageDistortionConfigRepo.uploadPendingConfigs() } returns false

        // When
        val result = fileUpSyncWorker.doWork()

        // Then
        Truth.assertThat(Result.retry()).isEqualTo(result)
        coVerify(exactly = 1) { imageDistortionConfigRepo.uploadPendingConfigs() }
        coVerify(exactly = 0) { imageRepository.uploadStoredImagesAndDelete(any()) }
    }

    @Test
    fun `doWork returns success when uploadStoredImagesAndDelete succeeds`() = runBlocking {
        // Given
        coEvery { imageDistortionConfigRepo.uploadPendingConfigs() } returns true
        coEvery { imageRepository.uploadStoredImagesAndDelete(PROJECT_ID) } returns true

        // When
        val result = fileUpSyncWorker.doWork()

        // Then
        Truth.assertThat(Result.success()).isEqualTo(result)
        coVerify(exactly = 1) { imageDistortionConfigRepo.uploadPendingConfigs() }
        coVerify(exactly = 1) { imageRepository.uploadStoredImagesAndDelete(PROJECT_ID) }
    }

    @Test
    fun `doWork returns retry when uploadStoredImagesAndDelete fails`() = runBlocking {
        // Given
        coEvery { imageDistortionConfigRepo.uploadPendingConfigs() } returns true
        coEvery { imageRepository.uploadStoredImagesAndDelete(PROJECT_ID) } returns false

        // When
        val result = fileUpSyncWorker.doWork()

        // Then
        Truth.assertThat(Result.retry()).isEqualTo(result)
        coVerify(exactly = 1) { imageDistortionConfigRepo.uploadPendingConfigs() }
        coVerify(exactly = 1) { imageRepository.uploadStoredImagesAndDelete(PROJECT_ID) }
    }

    @Test
    fun `doWork returns retry when an exception occurs`() = runBlocking {
        // Given
        coEvery { imageDistortionConfigRepo.uploadPendingConfigs() } throws RuntimeException("Test Exception")

        // When
        val result = fileUpSyncWorker.doWork()

        // Then
        Truth.assertThat(Result.retry()).isEqualTo(result)
        coVerify(exactly = 1) { imageDistortionConfigRepo.uploadPendingConfigs() }
        coVerify(exactly = 0) { imageRepository.uploadStoredImagesAndDelete(any()) }
    }
}
