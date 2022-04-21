package com.simprints.id.services.sync.images

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.id.services.sync.images.up.ImageUpSyncWorker
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ImageUpSyncWorkerTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()

    private lateinit var imageUpSyncWorker: ImageUpSyncWorker

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    @Before
    fun setUp() {
        imageUpSyncWorker = TestListenableWorkerBuilder<ImageUpSyncWorker>(app).build().apply {
            imageRepository = mockk()
            dispatcher = testDispatcherProvider
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

    private fun mockUploadResults(allUploadsSuccessful: Boolean) {
        coEvery {
            imageUpSyncWorker.imageRepository.uploadStoredImagesAndDelete()
        } returns allUploadsSuccessful
    }

}
