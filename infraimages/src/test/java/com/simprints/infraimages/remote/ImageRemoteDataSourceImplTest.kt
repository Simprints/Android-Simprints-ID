package com.simprints.infraimages.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.storage.FirebaseStorage
import com.simprints.core.sharedinterfaces.ImageUrlProvider
import com.simprints.infra.login.LoginManager
import com.simprints.infraimages.model.SecuredImageRef
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

@RunWith(AndroidJUnit4::class)
class ImageRemoteDataSourceImplTest {

    @Before
    fun setup() {
        // We need to mock statics and global extensions
        mockkStatic(FirebaseStorage::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun `test image upload flow`() = runTest() {

        val imgUrlProviderMock = mockk<ImageUrlProvider>() {
            coEvery { getImageStorageBucketUrl() } returns "gs://`simprints-dev.appspot.com"
        }

        val loginManagerMock = mockk<LoginManager>(relaxed = true) {
            every { getLegacyAppFallback() } returns mockk(relaxed = true)
            every { getLegacyAppFallback().options.projectId } returns "projectId"
        }

        val storageMock = mockk<FirebaseStorage>(relaxed = true) {
            every { reference } returns mockk()

            every { reference.child(any()) } returns mockk(relaxed = true) {
                every { reference.path } returns "testPath"

                coEvery { reference.putStream(any()).await() } coAnswers {
                    mockk {
                        every { task.isSuccessful } returns true
                    }
                }
            }
        }

        every { FirebaseStorage.getInstance(any(), any()) } returns storageMock

        val remoteDataSource = ImageRemoteDataSourceImpl(imgUrlProviderMock, loginManagerMock)
        val imageStreamMock = mockk<FileInputStream>(relaxed = true)
        
        val securedImageRefMock = mockk<SecuredImageRef>(relaxed = true) {
            every { relativePath.parts } returns arrayOf("Test1")
        }

        remoteDataSource.uploadImage(imageStreamMock, securedImageRefMock)
    }

    @Test
    fun `null project returns failed upload`() = runTest {

        val imgUrlProviderMock = mockk<ImageUrlProvider>()

        val loginManagerMock = mockk<LoginManager>(relaxed = true) {
            every { getLegacyAppFallback().options.projectId } returns null
        }

        val remoteDataSource = ImageRemoteDataSourceImpl(imgUrlProviderMock, loginManagerMock)
        val rtn = remoteDataSource.uploadImage(mockk(), mockk())

        assert(!rtn.isUploadSuccessful())
    }

    @Test
    fun `null storage bucket returns failed upload`() = runTest {

        val imgUrlProviderMock = mockk<ImageUrlProvider>() {
            coEvery { getImageStorageBucketUrl() } returns null
        }

        val loginManagerMock = mockk<LoginManager>(relaxed = true) {
            every { getLegacyAppFallback().options.projectId } returns "projectId"
        }

        val remoteDataSource = ImageRemoteDataSourceImpl(imgUrlProviderMock, loginManagerMock)
        val rtn = remoteDataSource.uploadImage(mockk(), mockk())

        assert(!rtn.isUploadSuccessful())
    }

}
