package com.simprints.infra.images.local

import androidx.security.crypto.EncryptedFile
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.security.SecurityManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class ImageLocalDataSourceImplTest {
    companion object {
        private const val PROJECT_ID = "projectId"
        private const val OTHER_PROJECT_ID = "otherProjectId"
    }

    @Test
    fun `check file directory is created`() {
        val path = "testpath"
        val file = File(path)

        ImageLocalDataSourceImpl(
            ctx = mockk {
                every { filesDir } returns file
            },
            mockk(),
            UnconfinedTestDispatcher(),
        )

        assert(File(path).exists())
    }

    @Test
    fun `check saving the file opens a file output`() = runTest {
        val file = File("testpath")
        val mockFile = mockk<EncryptedFile>()

        val encryptedFileMock = mockk<SecurityManager> {
            every { getEncryptedFileBuilder(any(), any()) } returns mockFile
        }

        val localSource = ImageLocalDataSourceImpl(
            ctx = mockk {
                every { filesDir } returns file
            },
            encryptedFileMock,
            UnconfinedTestDispatcher(),
        )

        val fileName = Path("testDir/Images")
        val imageBytes = byteArrayOf()
        localSource.encryptAndStoreImage(imageBytes, PROJECT_ID, fileName)

        verify(exactly = 1) { mockFile.openFileOutput() }
    }

    @Test
    fun `checking listing files without saving returns empty list`() = runTest {
        val file = File("testpath")
        val mockFile = mockk<EncryptedFile>()

        val encryptedFileMock = mockk<SecurityManager> {
            every { getEncryptedFileBuilder(any(), any()) } returns mockFile
        }

        val localSource = ImageLocalDataSourceImpl(
            ctx = mockk {
                every { filesDir } returns file
            },
            encryptedFileMock,
            UnconfinedTestDispatcher(),
        )

        val images = localSource.listImages(PROJECT_ID)

        assert(images.isEmpty())
    }

    @Test
    fun `checking decrypting the files opens the file stream`() = runTest {
        val file = File("testpath")
        val mockFile = mockk<EncryptedFile>()

        val encryptedFileMock = mockk<SecurityManager> {
            every { getEncryptedFileBuilder(any(), any()) } returns mockFile
        }

        val localSource = ImageLocalDataSourceImpl(
            ctx = mockk {
                every { filesDir } returns file
            },
            encryptedFileMock,
            UnconfinedTestDispatcher(),
        )

        val fileName = Path("testDir/Images")
        localSource.decryptImage(SecuredImageRef(fileName))

        verify(exactly = 1) { mockFile.openFileInput() }
    }

    @Test
    fun `check file delete deletes the dir`() = runTest {
        val path = "testpath"
        val file = File(path)

        val localSource = ImageLocalDataSourceImpl(
            ctx = mockk {
                every { filesDir } returns file
            },
            mockk(),
            UnconfinedTestDispatcher(),
        )

        localSource.deleteImage(SecuredImageRef(Path("$path/Image")))

        assert(!File("$path/Image").exists())
    }

    @Test
    fun `observeImages emits an initial empty list if no image files`() = runTest {
        val rootDir = Files.createTempDirectory("ImageLocalDataSourceImplTest").toFile()
        try {
            val localSource = createLocalSource(rootDir, UnconfinedTestDispatcher(testScheduler))
            val channel = Channel<List<SecuredImageRef>>(Channel.UNLIMITED)

            val collectJob = launch { localSource.observeImages(PROJECT_ID).collect { channel.trySend(it) } }

            val firstEmission = channel.receive()
            collectJob.cancel()
            assertThat(firstEmission).isEmpty()
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun `observeImages emits updated list after encryptAndStoreImage`() = runTest {
        val rootDir = Files.createTempDirectory("ImageLocalDataSourceImplTest").toFile()
        try {
            val localSource = createLocalSource(
                rootDir,
                UnconfinedTestDispatcher(testScheduler),
                securityManager = securityManagerWritingPlainFiles(),
            )
            val channel = Channel<List<SecuredImageRef>>(Channel.UNLIMITED)

            val collectJob = launch { localSource.observeImages(PROJECT_ID).collect { channel.trySend(it) } }

            val initial = channel.receive()

            val storedImage = localSource.encryptAndStoreImage(
                imageBytes = byteArrayOf(1, 2, 3),
                projectId = PROJECT_ID,
                relativePath = Path("subdir/file.png"),
            )

            require(storedImage != null)
            var updated: List<SecuredImageRef>
            do {
                updated = channel.receive()
            } while (!updated.contains(storedImage))
            collectJob.cancel()
            assertThat(initial).isEmpty()
            assertThat(updated).contains(storedImage)
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun `observeImages emits updated list after deleteImage`() = runTest {
        val rootDir = Files.createTempDirectory("ImageLocalDataSourceImplTest").toFile()
        try {
            val localSource = createLocalSource(
                rootDir,
                UnconfinedTestDispatcher(testScheduler),
                securityManager = securityManagerWritingPlainFiles(),
            )
            val channel = Channel<List<SecuredImageRef>>(Channel.UNLIMITED)

            val collectJob = launch { localSource.observeImages(PROJECT_ID).collect { channel.trySend(it) } }

            channel.receive() // initial listing
            val storedImage = localSource.encryptAndStoreImage(
                imageBytes = byteArrayOf(1, 2, 3),
                projectId = PROJECT_ID,
                relativePath = Path("subdir/file.png"),
            )
            require(storedImage != null)
            var afterStore: List<SecuredImageRef>
            do {
                afterStore = channel.receive()
            } while (!afterStore.contains(storedImage))

            localSource.deleteImage(storedImage)

            var afterDelete: List<SecuredImageRef>
            do {
                afterDelete = channel.receive()
            } while (afterDelete.contains(storedImage))
            collectJob.cancel()
            assertThat(afterStore).contains(storedImage)
            assertThat(afterDelete).doesNotContain(storedImage)
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun `observeImages does not include images from other projects`() = runTest {
        val rootDir = Files.createTempDirectory("ImageLocalDataSourceImplTest").toFile()
        try {
            val localSource = createLocalSource(
                rootDir,
                UnconfinedTestDispatcher(testScheduler),
                securityManager = securityManagerWritingPlainFiles(),
            )
            val otherProjectChannel = Channel<List<SecuredImageRef>>(Channel.UNLIMITED)
            val mainProjectChannel = Channel<List<SecuredImageRef>>(Channel.UNLIMITED)

            val otherProjectCollectJob =
                launch { localSource.observeImages(OTHER_PROJECT_ID).collect { otherProjectChannel.trySend(it) } }
            val mainProjectCollectJob =
                launch { localSource.observeImages(PROJECT_ID).collect { mainProjectChannel.trySend(it) } }

            val otherProjectInitial = otherProjectChannel.receive()
            mainProjectChannel.receive() // initial listing

            val storedImage = localSource.encryptAndStoreImage(
                imageBytes = byteArrayOf(1, 2, 3),
                projectId = PROJECT_ID,
                relativePath = Path("subdir/file.png"),
            )

            require(storedImage != null)
            var mainProjectAfterStore: List<SecuredImageRef>
            do {
                mainProjectAfterStore = mainProjectChannel.receive()
            } while (!mainProjectAfterStore.contains(storedImage))
            advanceUntilIdle()
            val otherProjectAfterInvalidation = otherProjectChannel.tryReceive().getOrNull()
            otherProjectCollectJob.cancel()
            mainProjectCollectJob.cancel()
            assertThat(otherProjectInitial).isEmpty()
            assertThat(otherProjectAfterInvalidation).isNull() // same value not re-emitted
            assertThat(mainProjectAfterStore).contains(storedImage)
        } finally {
            rootDir.deleteRecursively()
        }
    }

    private fun createLocalSource(
        filesDir: File,
        dispatcher: TestDispatcher,
        securityManager: SecurityManager = mockk(relaxed = true),
    ): ImageLocalDataSourceImpl = ImageLocalDataSourceImpl(
        ctx = mockk {
            every { this@mockk.filesDir } returns filesDir
        },
        keyHelper = securityManager,
        dispatcher = dispatcher,
    )

    private fun securityManagerWritingPlainFiles(): SecurityManager = mockk {
        every { getEncryptedFileBuilder(any(), any()) } answers {
            val file = firstArg<File>()
            mockk<EncryptedFile> {
                every { openFileOutput() } answers { file.outputStream() }
            }
        }
    }
}
