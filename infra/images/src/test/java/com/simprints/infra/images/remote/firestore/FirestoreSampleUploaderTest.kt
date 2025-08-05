package com.simprints.infra.images.remote.firestore

import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.google.firebase.storage.FirebaseStorage
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class FirestoreSampleUploaderTest {
    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var mockSecuredImageRef: SecuredImageRef

    @MockK
    private lateinit var metadataStore: ImageMetadataStore

    @MockK
    private lateinit var localDataSource: ImageLocalDataSource

    private lateinit var remoteDataSource: FirestoreSampleUploader

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { mockSecuredImageRef.relativePath.parts } returns arrayOf("Test1")

        remoteDataSource = FirestoreSampleUploader(
            configManager = configManager,
            authStore = authStore,
            localDataSource = localDataSource,
            metadataStore = metadataStore,
        )

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
    fun `null project returns failed upload`() = runTest {
        every { authStore.getLegacyAppFallback().options.projectId } returns null

        val result = remoteDataSource.uploadAllSamples(PROJECT_ID)

        assertThat(result).isFalse()
    }

    @Test
    fun `empty project returns failed upload`() = runTest {
        every { authStore.getLegacyAppFallback().options.projectId } returns ""

        val result = remoteDataSource.uploadAllSamples(PROJECT_ID)

        assertThat(result).isFalse()
    }

    @Test
    fun `when no samples to upload returns success`() = runTest {
        setupProjectConfig()
        setupStorageMock()
        configureLocalImageFiles(numberOfValidFiles = 0)

        coEvery { localDataSource.listImages(PROJECT_ID) } returns emptyList()

        assertThat(remoteDataSource.uploadAllSamples(PROJECT_ID)).isTrue()
    }

    @Test
    fun `test upload and delete all samples successfully`() = runTest {
        setupProjectConfig()
        setupStorageMock()
        configureLocalImageFiles(numberOfValidFiles = 3)

        assertThat(remoteDataSource.uploadAllSamples(PROJECT_ID)).isTrue()
        coVerify(exactly = 3) { localDataSource.decryptImage(any()) }
        coVerify(exactly = 3) { localDataSource.deleteImage(any()) }
        coVerify(exactly = 3) { metadataStore.deleteMetadata(any()) }
    }

    @Test
    fun `test upload and delete all samples with metadata successfully`() = runTest {
        setupProjectConfig()
        setupStorageMock()

        configureLocalImageFiles(numberOfValidFiles = 3, metadata = mapOf("k" to "v"))

        assertThat(remoteDataSource.uploadAllSamples(PROJECT_ID)).isTrue()
        coVerify(exactly = 3) { localDataSource.decryptImage(any()) }
        coVerify(exactly = 3) { localDataSource.deleteImage(any()) }
        coVerify(exactly = 3) { metadataStore.deleteMetadata(any()) }
    }

    @Test
    fun `test failed decryption should not return success`() = runTest {
        setupProjectConfig()
        setupStorageMock()

        coEvery { localDataSource.listImages(any()) } returns listOf(mockImage())
        coEvery { localDataSource.decryptImage(any()) } throws Exception("Cannot decrypt")

        assertThat(remoteDataSource.uploadAllSamples(PROJECT_ID)).isFalse()
    }

    @Test
    fun `test failed upload should not return success`() = runTest {
        setupProjectConfig()
        setupStorageMock(success = false)
        configureLocalImageFiles(numberOfValidFiles = 1)

        assertThat(remoteDataSource.uploadAllSamples(PROJECT_ID)).isFalse()
    }

    @Test
    fun `progress callback receives correct index counter values during upload`() = runTest {
        setupProjectConfig()
        setupStorageMock()
        configureLocalImageFiles(numberOfValidFiles = 3)
        val progressUpdates = mutableListOf<Pair<Int, Int>>()
        val progressCallback: suspend (Int, Int) -> Unit = { current, total ->
            progressUpdates.add(current to total)
        }

        assertThat(remoteDataSource.uploadAllSamples(PROJECT_ID, progressCallback)).isTrue()

        assertThat(progressUpdates).hasSize(3)
        assertThat(progressUpdates[0]).isEqualTo(0 to 3)
        assertThat(progressUpdates[1]).isEqualTo(1 to 3)
        assertThat(progressUpdates[2]).isEqualTo(2 to 3)
    }

    private fun setupProjectConfig() {
        coEvery { configManager.getProject(any()).imageBucket } returns "gs://`simprints-dev.appspot.com"
        every { authStore.getLegacyAppFallback().options.projectId } returns "projectId"
        every { authStore.signedInProjectId } returns "projectId"
    }

    private fun setupStorageMock(success: Boolean = true) {
        every { FirebaseStorage.getInstance(any(), any()) } returns mockk<FirebaseStorage>(relaxed = true) {
            every { reference.child(any()) } returns mockk {
                every { path } returns "testPath"
                every { putStream(any()) } returns mockk {
                    coEvery { await().task.isSuccessful } returns success
                }
                every { putStream(any(), any()) } returns mockk {
                    coEvery { await().task.isSuccessful } returns success
                }
            }
        }
    }

    private fun configureLocalImageFiles(
        numberOfValidFiles: Int,
        metadata: Map<String, String> = emptyMap(),
    ) {
        val files = mutableListOf<SecuredImageRef>().apply {
            repeat(numberOfValidFiles) {
                add(mockImage())
            }
        }
        coEvery { localDataSource.listImages(PROJECT_ID) } returns files

        val validImage = mockImage()
        val mockStream = mockk<FileInputStream>()

        coEvery {
            localDataSource.deleteImage(validImage)
        } returns true

        coEvery {
            localDataSource.decryptImage(validImage)
        } returns mockStream

        coEvery { metadataStore.getMetadata(any()) } returns metadata
        coJustRun { metadataStore.deleteMetadata(any()) }
    }

    private fun mockImage() = SecuredImageRef(Path(VALID_PATH))

    companion object {
        private const val VALID_PATH = "valid.txt"
        private const val PROJECT_ID = "projectId"
    }
}
