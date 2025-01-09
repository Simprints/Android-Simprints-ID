package com.simprints.fingerprint.infra.imagedistortionconfig.remote

import com.google.common.truth.Truth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
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

    @Test
    fun `uploadConfig returns false when firebase project name is null`() = runTest {
        every { authStore.getCoreApp().options.projectId } returns null

        val result = repo.uploadConfig(UN20_SERIAL_NUMBER, byteArrayOf())

        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `uploadConfig returns false when projectId is empty`() = runTest {
        every { authStore.getCoreApp().options.projectId } returns "firebaseProject"
        every { authStore.signedInProjectId } returns ""

        val result = repo.uploadConfig(UN20_SERIAL_NUMBER, byteArrayOf())

        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `uploadConfig returns true when file is already uploaded`() = runTest {
        val mockFileRef: StorageReference = mockk(relaxed = true)
        setupMockFirebase(mockFileRef)

        coEvery { mockFileRef.metadata.await() } returns mockk()
        val result = repo.uploadConfig(UN20_SERIAL_NUMBER, byteArrayOf())
        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `uploadConfig uploads file successfully`() = runTest {
        val mockFileRef: StorageReference = mockk(relaxed = true)
        val mockUploadTask: UploadTask.TaskSnapshot = mockk {
            every { task.isSuccessful } returns true
        }
        setupMockFirebase(mockFileRef)
        coEvery { mockFileRef.metadata.await() } throws mockk<StorageException> {
            every { errorCode } returns StorageException.ERROR_OBJECT_NOT_FOUND
        }
        coEvery { mockFileRef.putBytes(any()).await() } returns mockUploadTask

        val result = repo.uploadConfig(UN20_SERIAL_NUMBER, byteArrayOf(1, 2, 3))

        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `uploadConfig returns false when upload fails`() = runTest {
        val mockFileRef: StorageReference = mockk(relaxed = true)

        val mockUploadTask: UploadTask.TaskSnapshot = mockk {
            every { task.isSuccessful } returns false
        }

        setupMockFirebase(mockFileRef)
        coEvery { mockFileRef.putBytes(any()).await() } returns mockUploadTask
        coEvery { mockFileRef.metadata.await() } throws mockk<StorageException> {
            every { errorCode } returns StorageException.ERROR_OBJECT_NOT_FOUND
        }
        val result = repo.uploadConfig(UN20_SERIAL_NUMBER, byteArrayOf(1, 2, 3))

        Truth.assertThat(result).isFalse()
    }

    private fun setupMockFirebase(fileRef: StorageReference) {
        val bucketUrl = "bucket123"
        every { authStore.getCoreApp().options.projectId } returns "firebaseProject"
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { configManager.getProject(PROJECT_ID).imageBucket } returns bucketUrl

        val mockRootRef: StorageReference = mockk()
        mockkStatic(FirebaseStorage::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every {
            FirebaseStorage.getInstance(any(), bucketUrl).reference
        } returns mockRootRef
        every { mockRootRef.child("$PROJECTS_FOLDER/$PROJECT_ID/$UN20_MODULES_FOLDER/$UN20_SERIAL_NUMBER/$FILE_NAME") } returns fileRef
    }

    companion object {
        private const val PROJECTS_FOLDER = "projects"
        private const val UN20_MODULES_FOLDER = "un20modules"
        private const val UN20_SERIAL_NUMBER = "serial123"
        private const val PROJECT_ID = "project123"
        private const val FILE_NAME = "calibration.dat"
    }
}
