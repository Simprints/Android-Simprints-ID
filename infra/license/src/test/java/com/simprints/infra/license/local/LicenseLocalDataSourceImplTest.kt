package com.simprints.infra.license.local

import androidx.security.crypto.EncryptedFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.infra.security.SecurityManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LicenseLocalDataSourceImplTest {

    private val licenseVendor = "vendor1"

    @Test
    fun `check saving the file opens a file output`() = runTest {

        val filesDirPath = "testpath"
        val file = File(filesDirPath)
        val mockFile = mockk<EncryptedFile>()

        val encryptedFileMock = mockk<SecurityManager> {
            every { getEncryptedFileBuilder(any(), any()) } returns mockFile
        }

        val localSource = LicenseLocalDataSourceImpl(context = mockk {
            every { filesDir } returns file
        }, encryptedFileMock, UnconfinedTestDispatcher())


        val fileName = "testfile"
        localSource.saveLicense(licenseVendor, fileName)

        assert(File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}").exists())

        verify(exactly = 1) { mockFile.openFileOutput() }
    }

    @Test
    fun `check getting the file requests the created file`() = runTest {

        val file = File("testpath")
        val mockFile = mockk<EncryptedFile>()

        val encryptedFileMock = mockk<SecurityManager>() {
            every { getEncryptedFileBuilder(any(), any()) } returns mockFile
        }

        val localSource = LicenseLocalDataSourceImpl(context = mockk() {
            every { filesDir } returns file
        }, encryptedFileMock, UnconfinedTestDispatcher())

        localSource.getLicense(licenseVendor)

        verify(exactly = 1) { encryptedFileMock.getEncryptedFileBuilder(any(), any()) }
        verify(exactly = 1) { mockFile.openFileInput() }
    }

    @Test
    fun `check file delete deletes the dir`() = runTest {

        val path = "testpath"
        val file = File(path)

        val localsource = LicenseLocalDataSourceImpl(context = mockk() {
            every { filesDir } returns file
        }, mockk(), UnconfinedTestDispatcher())

        localsource.deleteCachedLicense()

        assert(!File("${path}/${LicenseLocalDataSource.LICENSES_FOLDER}/$licenseVendor").exists())
    }


}
