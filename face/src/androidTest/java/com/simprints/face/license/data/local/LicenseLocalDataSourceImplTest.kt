package com.simprints.face.license.data.local

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.license.local.LicenseLocalDataSourceImpl
import io.mockk.every
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*

@SmallTest
@RunWith(AndroidJUnit4::class)
class LicenseLocalDataSourceImplTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val filesFolder = "${app.filesDir}/${LicenseLocalDataSource.LICENSES_FOLDER}"
    private val filePath = "$filesFolder/${LicenseLocalDataSource.LICENSE_NAME}"
    private val licenseLocalDataSourceImpl = spyk(LicenseLocalDataSourceImpl(app))

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
