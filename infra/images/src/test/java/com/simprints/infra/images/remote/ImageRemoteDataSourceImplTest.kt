package com.simprints.infra.images.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.storage.FirebaseStorage
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.images.model.SecuredImageRef
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class ImageRemoteDataSourceImplTest {
    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var mockImageStream: FileInputStream

    @MockK
    private lateinit var mockSecuredImageRef: SecuredImageRef

    private lateinit var remoteDataSource: ImageRemoteDataSourceImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { mockSecuredImageRef.relativePath.parts } returns arrayOf("Test1")

        remoteDataSource = ImageRemoteDataSourceImpl(configManager, authStore)

        // We need to mock statics and global extensions
        mockkStatic(FirebaseStorage::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @After
    fun tearDown() {
        // Make sure static objects are unmocked
        unmockkStatic(FirebaseStorage::class)
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun `test image upload flow`() = runTest {
        coEvery { configManager.getProject(any()).imageBucket } returns "gs://`simprints-dev.appspot.com"
        every { authStore.getLegacyAppFallback().options.projectId } returns "projectId"
        every { authStore.signedInProjectId } returns "projectId"

        val storageMock = setupStorageMock()

        every { FirebaseStorage.getInstance(any(), any()) } returns storageMock

        val result = remoteDataSource.uploadImage(mockImageStream, mockSecuredImageRef, emptyMap())

        assertThat(result.isUploadSuccessful()).isTrue()
    }

    @Test
    fun `test image with metadata upload flow`() = runTest {
        coEvery { configManager.getProject(any()).imageBucket } returns "gs://`simprints-dev.appspot.com"
        every { authStore.getLegacyAppFallback().options.projectId } returns "projectId"
        every { authStore.signedInProjectId } returns "projectId"

        val storageMock = setupStorageMock()

        every { FirebaseStorage.getInstance(any(), any()) } returns storageMock

        val result = remoteDataSource.uploadImage(mockImageStream, mockSecuredImageRef, mapOf("key" to "value"))

        assertThat(result.isUploadSuccessful()).isTrue()
    }

    @Test
    fun `null project returns failed upload`() = runTest {
        every { authStore.getLegacyAppFallback().options.projectId } returns null

        val result = remoteDataSource.uploadImage(mockImageStream, mockSecuredImageRef, emptyMap())

        assertThat(result.isUploadSuccessful()).isFalse()
    }

    @Test
    fun `null storage bucket returns failed upload`() = runTest {
        coEvery { configManager.getProject(any()).imageBucket } returns ""
        every { authStore.getLegacyAppFallback().options.projectId } returns "projectId"

        val result = remoteDataSource.uploadImage(mockImageStream, mockSecuredImageRef, emptyMap())

        assertThat(result.isUploadSuccessful()).isFalse()
    }

    private fun setupStorageMock() = mockk<FirebaseStorage>(relaxed = true) {
        every { reference.child(any()) } returns mockk {
            every { path } returns "testPath"
            every { putStream(any()) } returns mockk {
                coEvery { await() } returns mockk {
                    every { task } returns mockk {
                        every { isSuccessful } returns true
                    }
                }
            }
            every { putStream(any(), any()) } returns mockk {
                coEvery { await() } returns mockk {
                    every { task } returns mockk {
                        every { isSuccessful } returns true
                    }
                }
            }
        }
    }
}
