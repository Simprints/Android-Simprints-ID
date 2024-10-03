package com.simprints.fingerprint.infra.necsdkimpl.initialization

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.SaveLicenseCheckEventUseCase
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import com.simprints.necwrapper.nec.NEC
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SdkInitializerImplTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var nec: NEC

    @MockK
    lateinit var licenseRepository: LicenseRepository

    @MockK
    lateinit var saveLicenseCheck: SaveLicenseCheckEventUseCase

    private lateinit var sdkInitializer: SdkInitializer<Unit>



    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        justRun { nec.init(any(), context) }
        coEvery {
            licenseRepository.getCachedLicense(Vendor.Nec)
        } returns License("2133-12-30T17:32:28Z", "license", LicenseVersion("1.0"))

        coJustRun { licenseRepository.deleteCachedLicense(Vendor.Nec) }
        sdkInitializer =
            SdkInitializerImpl(context, nec, licenseRepository, saveLicenseCheck)
    }

    @Test
    fun `test initialize success`() = runTest {
        //Given
        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheck(Vendor.Nec, capture(licenseStatusSlot)) }

        // When
        sdkInitializer.initialize(null)
        // Then
        verify { nec.init(any(), context) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.VALID)
    }

    @Test(expected = BioSdkException.BioSdkInitializationException::class)
    fun `test initialize with expired license`() = runTest {
        //Given
        coEvery {
            licenseRepository.getCachedLicense(Vendor.Nec)
        } returns License("2011-12-30T17:32:28Z", "license", LicenseVersion("1.0"))
        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheck(Vendor.Nec, capture(licenseStatusSlot)) }

        // When
        sdkInitializer.initialize(null)
        // Then
        coVerify { licenseRepository.deleteCachedLicense(Vendor.Nec) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.EXPIRED)
    }

    @Test(expected = BioSdkException.BioSdkInitializationException::class)
    fun `test error during initialization`() = runTest {
        //Given
        every { nec.init(any(),context) } throws Exception()
        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheck(Vendor.Nec, capture(licenseStatusSlot)) }

        // When
        sdkInitializer.initialize(null)
        // Then
        coVerify { licenseRepository.deleteCachedLicense(Vendor.Nec) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.ERROR)
    }
}
