package com.simprints.infra.license.local

import androidx.security.crypto.EncryptedFile
import com.simprints.infra.security.SecurityManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.io.File

class LicenseLocalDataSourceImplTest {

    @Test
    fun `check file directory is created`() {

        val path = "testpath"
        val file = File(path)

        LicenseLocalDataSourceImpl(context = mockk() {
            every { filesDir } returns file
        }, mockk())

        assert(File("${path}/${LicenseLocalDataSource.LICENSES_FOLDER}").exists())
    }

    @Test
    fun `check saving the file opens a file output`() {

        val file = File("testpath")
        val mockFile = mockk<EncryptedFile>()

        val encryptedFileMock = mockk<SecurityManager> {
            every { getEncryptedFileBuilder(any(), any()) } returns mockFile
        }

        val localSource = LicenseLocalDataSourceImpl(context = mockk {
            every { filesDir } returns file
        }, encryptedFileMock)


        val fileName = "testfile"
        localSource.saveLicense(fileName)

        verify(exactly = 1) { mockFile.openFileOutput() }
    }

    @Test
    fun `check getting the file requests the created file`() {

        val file = File("testpath")
        val mockFile = mockk<EncryptedFile>()

        val encryptedFileMock = mockk<SecurityManager>() {
            every { getEncryptedFileBuilder(any(), any()) } returns mockFile
        }

        val localSource = LicenseLocalDataSourceImpl(context = mockk() {
            every { filesDir } returns file
        }, encryptedFileMock)

        localSource.getLicense()

        verify(exactly = 1) { encryptedFileMock.getEncryptedFileBuilder(any(), any()) }
        verify(exactly = 1) { mockFile.openFileInput() }
    }

    @Test
    fun `check file delete deletes the dir`() {

        val path = "testpath"
        val file = File(path)

        val localsource = LicenseLocalDataSourceImpl(context = mockk() {
            every { filesDir } returns file
        }, mockk())

        localsource.deleteCachedLicense()

        assert(!File("${path}/${LicenseLocalDataSource.LICENSES_FOLDER}/${LicenseLocalDataSource.LICENSE_NAME}").exists())
    }


}
