package com.simprints.fingerprint.infra.necsdkimpl.initialization

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.Vendor
import com.simprints.necwrapper.nec.NEC
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
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


    private lateinit var sdkInitializer: SdkInitializer<Unit>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        justRun { nec.init(any(), context) }
        coJustRun { licenseRepository.deleteCachedLicense(Vendor.NEC_FINGERPRINT_VENDOR) }

        sdkInitializer =
            SdkInitializerImpl(context,  nec, licenseRepository)
    }

    @Test
    fun `test initialize success`() = runTest {
        //Given
        coEvery {
            licenseRepository.getCachedLicense(Vendor.NEC_FINGERPRINT_VENDOR)
        } returns "license"

        // When
        sdkInitializer.initialize(null)
        // Then
        verify { nec.init(any(), context) }
    }

    @Test(expected = BioSdkException.BioSdkInitializationException::class)
    fun `test initialize with expired license`() = runTest {
        //Given
        coEvery {
            licenseRepository.getCachedLicense(Vendor.NEC_FINGERPRINT_VENDOR)
        } returns  "license"

        every { nec.init(any(),context) } throws Exception()

        // When
        sdkInitializer.initialize(null)
        // Then
        coVerify { licenseRepository.deleteCachedLicense(Vendor.NEC_FINGERPRINT_VENDOR) }
    }

}
