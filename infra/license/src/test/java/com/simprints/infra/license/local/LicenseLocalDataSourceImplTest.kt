package com.simprints.infra.license.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.security.crypto.EncryptedFile
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class LicenseLocalDataSourceImplTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var encryptedFile: EncryptedFile

    private val licenseVendor = Vendor.RankOne
    private val licenceVersion = "1.1"
    private val filesDirPath = "testpath"

    private lateinit var localSource: LicenseLocalDataSourceImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { context.filesDir } returns File(filesDirPath)
        every { securityManager.getEncryptedFileBuilder(any(), any()) } returns encryptedFile

        localSource = LicenseLocalDataSourceImpl(
            context = context,
            keyHelper = securityManager,
            dispatcherIo = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `check saving the file opens a file output`() = runTest {
        val fileName = "testfile"
        val expirationDate = "2023-01-01"
        localSource.saveLicense(licenseVendor, License(expirationDate, fileName, LicenseVersion.UNLIMITED))

        assert(File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}").exists())
        verify(exactly = 1) { encryptedFile.openFileOutput() }
    }

    @Test
    fun `check saving the file with version opens a file output`() = runTest {
        val licenceData = "testfile"
        val expirationDate = "2023-01-01"
        localSource.saveLicense(licenseVendor, License(expirationDate, licenceData, LicenseVersion(licenceVersion)))

        assertThat(File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/${licenseVendor.value}").exists()).isTrue()
        assertThat(File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/${licenseVendor.value}").isDirectory).isTrue()
        verify(exactly = 1) { encryptedFile.openFileOutput() }
    }

    @Test
    fun `check getting the file for version requests the file`() = runTest {
        File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/${licenseVendor.value}").mkdirs()
        File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/${licenseVendor.value}/$licenceVersion").createNewFile()

        localSource.getLicense(licenseVendor)

        verify(exactly = 1) { securityManager.getEncryptedFileBuilder(any(), any()) }
        verify(exactly = 1) { encryptedFile.openFileInput() }
    }

    @Test
    fun `check getting the file for version requests the file with highest version`() = runTest {
        File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/${licenseVendor.value}").mkdirs()
        File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/${licenseVendor.value}/0.9").createNewFile()
        File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/${licenseVendor.value}/$licenceVersion").createNewFile()
        File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/${licenseVendor.value}/1.2").createNewFile()

        localSource.getLicense(licenseVendor)

        verify(exactly = 1) {
            securityManager.getEncryptedFileBuilder(
                withArg {
                    assertThat(it.name).isEqualTo("1.2")
                },
                any(),
            )
        }
        verify(exactly = 1) { encryptedFile.openFileInput() }
    }

    @Test
    fun `check getting the file requests the created file`() = runTest {
        localSource.getLicense(licenseVendor)

        verify(exactly = 1) { securityManager.getEncryptedFileBuilder(any(), any()) }
        verify(exactly = 1) { encryptedFile.openFileInput() }
    }

    @Test
    fun `check file delete deletes the dir`() = runTest {
        localSource.deleteCachedLicense(licenseVendor)

        assertThat(File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/$licenseVendor").exists()).isFalse()
    }

    @Test
    fun `check delete all deletes the dir`() = runTest {
        localSource.deleteCachedLicenses()

        assertThat(File("$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}/$licenseVendor").exists()).isFalse()
    }

    @Test
    fun `check getting the file renames old Roc license file to RANK_ONE_FACE `() = runTest {
        // Create the license folder and the old ROC.lic file
        val licenceFolderPath = "$filesDirPath/${LicenseLocalDataSource.LICENSES_FOLDER}"
        File(licenceFolderPath).mkdirs()
        val oldRocLicenseFile = File("$licenceFolderPath/ROC.lic")
        oldRocLicenseFile.createNewFile()
        val newRocLicenseFile = File("$licenceFolderPath/RANK_ONE_FACE")

        localSource.getLicense(licenseVendor)

        // Check that the old ROC.lic file has been renamed to RANK_ONE_FACE
        assertThat(oldRocLicenseFile.exists()).isFalse()
        assertThat(newRocLicenseFile.exists()).isTrue()
    }

    @Test
    fun `check getting the file returns null if file does not exist`() = runTest {
        val license = localSource.getLicense(licenseVendor)

        assertThat(license).isNull()
    }

    @After
    fun tearDown() {
        File(filesDirPath).deleteRecursively()
    }
}
