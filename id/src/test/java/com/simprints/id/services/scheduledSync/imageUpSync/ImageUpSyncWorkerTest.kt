package com.simprints.id.services.scheduledSync.imageUpSync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.id.testtools.TestApplication
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ImageUpSyncWorkerTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()

    private lateinit var imageUpSyncWorker: ImageUpSyncWorker

    @Before
    fun setUp() {
        imageUpSyncWorker = TestListenableWorkerBuilder<ImageUpSyncWorker>(app).build().apply {
            imageRepository = mockk()
            crashReportManager = mockk(relaxed = true)
            baseUrlProvider = mockk()
        }
        app.component = mockk(relaxed = true)
    }

    @Test
    fun whenAllUploadsAreSuccessful_shouldReturnSuccess() = runBlocking {
        mockUploadResults(allUploadsSuccessful = true)

        val result = imageUpSyncWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun whenNotAllUploadsAreSuccessful_shouldReturnRetry() = runBlocking {
        mockUploadResults(allUploadsSuccessful = false)

        val result = imageUpSyncWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun shouldFetchImageStorageBucketUrlFromBaseUrlProvider() = runBlocking {
        imageUpSyncWorker.doWork()

        verify { imageUpSyncWorker.baseUrlProvider.getImageStorageBucketUrl() }
    }

    private fun mockUploadResults(allUploadsSuccessful: Boolean) {
        coEvery {
            imageUpSyncWorker.imageRepository.uploadStoredImagesAndDelete(any())
        } returns allUploadsSuccessful
    }

}
