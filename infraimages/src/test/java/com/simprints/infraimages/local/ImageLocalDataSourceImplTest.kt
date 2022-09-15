package com.simprints.infraimages.local

import androidx.security.crypto.EncryptedFile
import com.simprints.infra.security.MasterKeyHelper
import com.simprints.infraimages.model.Path
import com.simprints.infraimages.model.SecuredImageRef
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.io.File

class ImageLocalDataSourceImplTest {

    @Test
    fun `check file directory is created`() {

        val path = "testpath"
        val file = File(path)

        ImageLocalDataSourceImpl(ctx = mockk() {
            every { filesDir } returns file
        }, mockk())

        assert(File(path).exists())
    }

    @Test
    fun `check saving the file opens a file output`() {

        val file = File("testpath")
        val mockFile = mockk<EncryptedFile>()

        val encryptedFileMock = mockk<MasterKeyHelper>() {
            every { getEncryptedFileBuilder(any(), any()) } returns mockFile
        }

        val localSource = ImageLocalDataSourceImpl(ctx = mockk() {
            every { filesDir } returns file
        }, encryptedFileMock)


        val fileName = Path("testDir/Images")
        val imageBytes = byteArrayOf()
        localSource.encryptAndStoreImage(imageBytes, fileName)

        verify(exactly = 1) { mockFile.openFileOutput() }
    }

    @Test
    fun `checking listing files without saving returns empty list`() {

        val file = File("testpath")
        val mockFile = mockk<EncryptedFile>()

        val encryptedFileMock = mockk<MasterKeyHelper>() {
            every { getEncryptedFileBuilder(any(), any()) } returns mockFile
        }

        val localSource = ImageLocalDataSourceImpl(ctx = mockk() {
            every { filesDir } returns file
        }, encryptedFileMock)

        val images = localSource.listImages()

       assert(images.isEmpty())
    }

    @Test
    fun `check file delete deletes the dir`() {

        val path = "testpath"
        val file = File(path)

        val localsource = ImageLocalDataSourceImpl(ctx = mockk() {
            every { filesDir } returns file
        }, mockk())

        localsource.deleteImage(SecuredImageRef(Path("${path}/Image")))

        assert(!File("${path}/Image").exists())
    }

}
