package com.simprints.fingerprint.infra.imagedistortionconfig.remote

import com.google.common.truth.Truth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImageDistortionConfigRemoteRepoTest {
    private lateinit var repo: ImageDistortionConfigRemoteRepo

    private val configManager: ConfigManager = mockk()
    private val authStore: AuthStore = mockk()

    @Before
    fun setUp() {
        repo = ImageDistortionConfigRemoteRepo(
            configManager = configManager,
            authStore = authStore,
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseStorage::class)
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun `uploadConfig returns false when firebase project name is null`() = runTest {
        every { authStore.getCoreApp().options.projectId } returns null

        val result = repo.uploadConfig(SERIAL_NUMBER, byteArrayOf())

        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `uploadConfig returns false when projectId is empty`() = runTest {
        every { authStore.getCoreApp().options.projectId } returns "firebaseProject"
        every { authStore.signedInProjectId } returns ""

        val result = repo.uploadConfig(SERIAL_NUMBER, byteArrayOf())

        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `uploadConfig returns true when file is already uploaded`() = runTest {
        val mockFolderRef: StorageReference = mockk(relaxed = true)
        val mockListResult: ListResult = mockk {
            every { items } returns listOf(mockk())
        }

        setupMockFirebase(mockFolderRef)
        coEvery { mockFolderRef.listAll().await() } returns mockListResult

        val result = repo.uploadConfig(SERIAL_NUMBER, byteArrayOf())

        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `uploadConfig uploads file successfully`() = runTest {
        val mockFolderRef: StorageReference = mockk(relaxed = true)
        val mockFileRef: StorageReference = mockk(relaxed = true)
        val mockListResult: ListResult = mockk {
            every { items } returns emptyList()
        }
        val mockUploadTask: UploadTask.TaskSnapshot = mockk {
            every { task.isSuccessful } returns true
        }

        setupMockFirebase(mockFolderRef)
        coEvery { mockFolderRef.listAll().await() } returns mockListResult
        every { mockFolderRef.child(FILE_NAME) } returns mockFileRef
        coEvery { mockFileRef.putBytes(any()).await() } returns mockUploadTask

        val result = repo.uploadConfig(SERIAL_NUMBER, byteArrayOf(1, 2, 3))

        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `uploadConfig returns false when upload fails`() = runTest {
        val mockFolderRef: StorageReference = mockk(relaxed = true)
        val mockFileRef: StorageReference = mockk(relaxed = true)
        val mockListResult: ListResult = mockk {
            every { items } returns emptyList()
        }
        val mockUploadTask: UploadTask.TaskSnapshot = mockk {
            every { task.isSuccessful } returns false
        }

        setupMockFirebase(mockFolderRef)
        coEvery { mockFolderRef.listAll().await() } returns mockListResult
        every { mockFolderRef.child(FILE_NAME) } returns mockFileRef
        coEvery { mockFileRef.putBytes(any()).await() } returns mockUploadTask

        val result = repo.uploadConfig(SERIAL_NUMBER, byteArrayOf(1, 2, 3))

        Truth.assertThat(result).isFalse()
    }

    private fun setupMockFirebase(folderRef: StorageReference) {
        val bucketUrl = "bucket123"
        every { authStore.getCoreApp().options.projectId } returns "firebaseProject"
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { configManager.getProject(PROJECT_ID).imageBucket } returns bucketUrl

        val mockRootRef: StorageReference = mockk(relaxed = true)
        mockkStatic(FirebaseStorage::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every {
            FirebaseStorage.getInstance(any(), bucketUrl).reference
        } returns mockRootRef
        every { mockRootRef.child("$PROJECTS_FOLDER/$PROJECT_ID/$UN20_MODULES_FOLDER/$SERIAL_NUMBER/") } returns folderRef
    }

    companion object {
        private const val PROJECTS_FOLDER = "projects"
        private const val UN20_MODULES_FOLDER = "un20modules"
        private const val SERIAL_NUMBER = "serial123"
        private const val PROJECT_ID = "project123"
        private const val FILE_NAME = "calibration.dat"
    }
}
