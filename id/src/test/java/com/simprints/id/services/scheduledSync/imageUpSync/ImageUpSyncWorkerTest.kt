package com.simprints.id.services.scheduledSync.imageUpSync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestDataModule
import com.simprints.id.data.db.image.remote.UploadResult
import com.simprints.id.data.db.image.repository.ImageRepository
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.wheneverOnSuspend
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ImageUpSyncWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var repository: ImageRepository

    private lateinit var imageUpSyncWorker: ImageUpSyncWorker

    private val appModule by lazy {
        TestAppModule(app)
    }

    private val dataModule by lazy {
        TestDataModule(
            imageLocalDataSourceRule = DependencyRule.MockRule,
            imageRepositoryRule = DependencyRule.MockRule
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, appModule = appModule, dataModule = dataModule).fullSetup()
        MockitoAnnotations.initMocks(this)
        imageUpSyncWorker = TestListenableWorkerBuilder<ImageUpSyncWorker>(app).build()
    }

    @Test
    fun whenAllUploadsAreSuccessful_shouldReturnSuccess() = runBlockingTest {
        mockUploadResults()

        val workResult = imageUpSyncWorker.doWork()

        assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun whenAnyUploadFails_shouldReturnRetry() = runBlockingTest {
        mockUploadResults(addFailedUpload = true)

        val workResult = imageUpSyncWorker.doWork()

        assertThat(workResult).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun withNoImagesToUpload_shouldReturnSuccess() = runBlockingTest {
        wheneverOnSuspend(repository) {
            uploadImages()
        } thenOnBlockingReturn emptyList()

        val workResult = imageUpSyncWorker.doWork()

        assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
    }

    private fun mockUploadResults(addFailedUpload: Boolean = false) {
        val uploadResults = mutableListOf(
            UploadResult(SecuredImageRef(PATH), UploadResult.Status.SUCCESSFUL),
            UploadResult(SecuredImageRef(PATH), UploadResult.Status.SUCCESSFUL),
            UploadResult(SecuredImageRef(PATH), UploadResult.Status.SUCCESSFUL),
            UploadResult(SecuredImageRef(PATH), UploadResult.Status.SUCCESSFUL),
            UploadResult(SecuredImageRef(PATH), UploadResult.Status.SUCCESSFUL)
        ).apply {
            if (addFailedUpload)
                add(UploadResult(SecuredImageRef(PATH), UploadResult.Status.FAILED))
        }

        wheneverOnSuspend(repository) {
            uploadImages()
        } thenOnBlockingReturn uploadResults
    }

    companion object {
        private const val PATH = "mock/path"
    }

}
