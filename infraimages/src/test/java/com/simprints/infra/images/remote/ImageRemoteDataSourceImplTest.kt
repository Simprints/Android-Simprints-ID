package com.simprints.infra.images.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.storage.FirebaseStorage
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.login.LoginManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class ImageRemoteDataSourceImplTest {

    @Before
    fun setup() {
        // We need to mock statics and global extensions
        mockkStatic(FirebaseStorage::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }


    @Test
    fun `test image upload flow`() = runTest {

        val imgUrlProviderMock = mockk<ConfigManager> {
            coEvery { getProject(any()).imageBucket } returns "gs://`simprints-dev.appspot.com"
        }

        val loginManagerMock = mockk<LoginManager>(relaxed = true) {
            every { getLegacyAppFallback() } returns mockk(relaxed = true)
            every { getLegacyAppFallback().options.projectId } returns "projectId"
            every { getSignedInProjectIdOrEmpty() } returns "projectId"
        }

        val storageMock = setupStorageMock()

        every { FirebaseStorage.getInstance(any(), any()) } returns storageMock

        val remoteDataSource = ImageRemoteDataSourceImpl(imgUrlProviderMock, loginManagerMock)
        val imageStreamMock = mockk<FileInputStream>(relaxed = true)

        val securedImageRefMock = mockk<SecuredImageRef>(relaxed = true) {
            every { relativePath.parts } returns arrayOf("Test1")
        }

        val result = remoteDataSource.uploadImage(imageStreamMock, securedImageRefMock)

        assert(result.isUploadSuccessful())
    }

    @Test
    fun `null project returns failed upload`() = runTest {

        val imgUrlProviderMock = mockk<ConfigManager>()

        val loginManagerMock = mockk<LoginManager>(relaxed = true) {
            every { getLegacyAppFallback().options.projectId } returns null
        }

        val remoteDataSource = ImageRemoteDataSourceImpl(imgUrlProviderMock, loginManagerMock)
        val rtn = remoteDataSource.uploadImage(mockk(), mockk())

        assert(!rtn.isUploadSuccessful())
    }

    @Test
    fun `null storage bucket returns failed upload`() = runTest {

        val imgUrlProviderMock = mockk<ConfigManager> {
            coEvery { getProject(any()).imageBucket } returns ""
        }

        val loginManagerMock = mockk<LoginManager>(relaxed = true) {
            every { getLegacyAppFallback().options.projectId } returns "projectId"
        }

        val remoteDataSource = ImageRemoteDataSourceImpl(imgUrlProviderMock, loginManagerMock)
        val rtn = remoteDataSource.uploadImage(mockk(), mockk())

        assert(!rtn.isUploadSuccessful())
    }

    private fun setupStorageMock() = mockk<FirebaseStorage>(relaxed = true) {
        every { reference } returns mockk {
            every { child(any()) } returns mockk {
                every { path } returns "testPath"
                every { putStream(any()) } returns mockk {
                    coEvery { await() } returns mockk {
                        every { task } returns mockk {
                            every { isSuccessful } returns true
                        }
                    }
                }
            }
        }
    }

}
