package com.simprints.id.services.scheduledSync.imageUpSync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestDataModule
import com.simprints.id.data.db.image.repository.ImageRepository
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.wheneverOnSuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ImageUpSyncWorkerTest {

    @Inject lateinit var repository: ImageRepository

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private val appModule by lazy {
        TestAppModule(app)
    }

    private val dataModule by lazy {
        TestDataModule(
            imageLocalDataSourceRule = DependencyRule.MockRule,
            imageRepositoryRule = DependencyRule.MockRule
        )
    }

    private lateinit var imageUpSyncWorker: ImageUpSyncWorker

    @Before
    fun setUp() {
        UnitTestConfig(this, appModule = appModule, dataModule = dataModule).fullSetup()
        imageUpSyncWorker = TestListenableWorkerBuilder<ImageUpSyncWorker>(app).build()
    }

    @Test
    fun whenAllUploadsAreSuccessful_shouldReturnSuccess() = runBlockingTest {
        mockUploadResults(allUploadsSuccessful = true)

        val result = imageUpSyncWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun whenNotAllUploadsAreSuccessful_shouldReturnRetry() = runBlockingTest {
        mockUploadResults(allUploadsSuccessful = false)

        val result = imageUpSyncWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    private fun mockUploadResults(allUploadsSuccessful: Boolean) {
        wheneverOnSuspend(repository) {
            uploadStoredImagesAndDelete()
        } thenOnBlockingReturn allUploadsSuccessful
    }

}
