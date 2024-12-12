package com.simprints.infra.images.local

import androidx.security.crypto.EncryptedFile
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.security.SecurityManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class ImageLocalDataSourceImplTest {
    companion object {
        private const val PROJECT_ID = "projectId"
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
}
