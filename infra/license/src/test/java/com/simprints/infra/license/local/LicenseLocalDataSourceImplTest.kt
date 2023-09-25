package com.simprints.infra.license.local

import androidx.security.crypto.EncryptedFile
import com.simprints.infra.license.Vendor
import com.simprints.infra.security.SecurityManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import java.io.File

class LicenseLocalDataSourceImplTest {

    private val licenseVendor = Vendor("vendor1")
    private val filesDirPath = "testpath"

    @Test
    fun `check saving the file opens a file output`() = runTest {

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

        val file = File(filesDirPath)
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

        val file = File(filesDirPath)

        val localSource = LicenseLocalDataSourceImpl(context = mockk() {
            every { filesDir } returns file
        }, mockk(), UnconfinedTestDispatcher())

        localSource.deleteCachedLicense()

        assert(!File("${filesDirPath}/${LicenseLocalDataSource.LICENSES_FOLDER}/$licenseVendor").exists())
    }

    @Test
    fun `check getting the file renames old Roc license file to RANK_ONE_FACE `() = runTest {

        // Create the license folder and the old ROC.lic file
        File("${filesDirPath}/${LicenseLocalDataSource.LICENSES_FOLDER}").mkdirs()
        val oldRocLicenseFile =
            File("${filesDirPath}/${LicenseLocalDataSource.LICENSES_FOLDER}/ROC.lic")
        oldRocLicenseFile.createNewFile()
        val newRocLicenseFile =
            File("${filesDirPath}/${LicenseLocalDataSource.LICENSES_FOLDER}/RANK_ONE_FACE")

        val localSource = LicenseLocalDataSourceImpl(context = mockk() {
            every { filesDir } returns File(filesDirPath)
        }, mockk(), UnconfinedTestDispatcher())

        localSource.getLicense(licenseVendor)

        // Check that the old ROC.lic file has been renamed to RANK_ONE_FACE
        assert(!oldRocLicenseFile.exists())
        assert(newRocLicenseFile.exists())


    }

    @Test
    fun `check getting the file returns null if file does not exist`() = runTest {

        val localSource = LicenseLocalDataSourceImpl(context = mockk {
            every { filesDir } returns File(filesDirPath)
        }, mockk(), UnconfinedTestDispatcher())

        val license = localSource.getLicense(licenseVendor)

        assert(license == null)
    }

    @After
    fun tearDown() {
        File(filesDirPath).deleteRecursively()
    }
}
