package com.simprints.infralicense

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.simprints.infralicense.local.LicenseLocalDataSource
import com.simprints.infralicense.local.LicenseLocalDataSourceImpl
import io.mockk.every
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.*

@SmallTest
class LicenseLocalDataSourceImplTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val filesFolder = "${app.filesDir}/${LicenseLocalDataSource.LICENSES_FOLDER}"
    private val filePath = "$filesFolder/${LicenseLocalDataSource.LICENSE_NAME}"
    private val licenseLocalDataSourceImpl = spyk(LicenseLocalDataSourceImpl(app, MasterKeyHelper()))

    @Before
    fun setUp() {
        File(filesFolder).deleteRecursively()
    }

    @Test
    fun givenALicense_storeItEncrypted() {
        val license = UUID.randomUUID().toString()
        licenseLocalDataSourceImpl.saveLicense(license)
        val savedLicense = String(File(filePath).readBytes())

        assertThat(savedLicense).isNotEqualTo(license)
    }

    @Test
    fun givenAnEncryptedFile_decryptIt_shouldReturnTheRightContent() {
        val license = UUID.randomUUID().toString()
        licenseLocalDataSourceImpl.saveLicense(license)

        val licenseRead = licenseLocalDataSourceImpl.getLicense()
        assertThat(licenseRead).isEqualTo(license)
    }

    @Test
    fun ifLicenseInexistent_returnNull() {
        every { licenseLocalDataSourceImpl.getFileFromAssets() } returns null
        val licenseRead = licenseLocalDataSourceImpl.getLicense()
        assertThat(licenseRead).isNull()
    }
}
