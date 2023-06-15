package com.simprints.infra.license

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.license.local.LicenseLocalDataSource
import com.simprints.infra.license.local.LicenseLocalDataSourceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import java.io.File
import java.util.*

class LicenseLocalDataSourceImplTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val filesFolder = "${app.filesDir}/${LicenseLocalDataSource.LICENSES_FOLDER}"
    private val filePath = "$filesFolder/${LicenseLocalDataSource.LICENSE_NAME}"
    private val licenseLocalDataSourceImpl = spyk(
        LicenseLocalDataSourceImpl(app, mockk(), UnconfinedTestDispatcher())
    )

    @Before
    fun setUp() {
        File(filesFolder).deleteRecursively()
    }

    @Test
    fun givenALicense_storeItEncrypted() = runTest {
        val license = UUID.randomUUID().toString()
        licenseLocalDataSourceImpl.saveLicense(license)
        val savedLicense = String(File(filePath).readBytes())

        assertThat(savedLicense).isNotEqualTo(license)
    }

    @Test
    fun givenAnEncryptedFile_decryptIt_shouldReturnTheRightContent() = runTest {
        val license = UUID.randomUUID().toString()
        licenseLocalDataSourceImpl.saveLicense(license)

        val licenseRead = licenseLocalDataSourceImpl.getLicense()
        assertThat(licenseRead).isEqualTo(license)
    }

    @Test
    fun ifLicenseInexistent_returnNull() = runTest {
        every { licenseLocalDataSourceImpl.getFileFromAssets() } returns null
        val licenseRead = licenseLocalDataSourceImpl.getLicense()
        assertThat(licenseRead).isNull()
    }
}
