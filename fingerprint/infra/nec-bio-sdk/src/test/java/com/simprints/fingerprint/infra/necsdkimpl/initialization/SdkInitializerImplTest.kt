package com.simprints.fingerprint.infra.necsdkimpl.initialization

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseState
import com.simprints.necwrapper.nec.NEC
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.justRun
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
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

    @RelaxedMockK
    lateinit var authStore: AuthStore


    private lateinit var sdkInitializer: SdkInitializer<Unit>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        justRun { nec.init(any(), context) }
        coJustRun { licenseRepository.deleteCachedLicense() }

        sdkInitializer =
            SdkInitializerImpl(context, "DEVICE_ID", nec, licenseRepository, authStore)
    }

    @Test
    fun `test initialize success`() = runTest {
        //Given
        coEvery {
            licenseRepository.getLicenseStates(any(), any(), any())
        } returns listOf(
            LicenseState.Started,
            LicenseState.FinishedWithSuccess("license")
        ).asFlow()

        // When
        sdkInitializer.initialize(null)
        // Then
        verify { nec.init(any(), context) }
    }

    @Test(expected = BioSdkException.BioSdkInitializationException::class)
    fun `test initialize with expired license`() = runTest {
        //Given
        coEvery {
            licenseRepository.getLicenseStates(any(), any(), any())
        } returns listOf(
            LicenseState.Started,
            LicenseState.FinishedWithSuccess("license")
        ).asFlow()
        every { nec.init(any(),context) } throws Exception()

        // When
        sdkInitializer.initialize(null)
        // Then
        coVerify { licenseRepository.deleteCachedLicense() }
    }
    @Test(expected = BioSdkException.LicenseDownloadException::class)
    fun `test initialize failure to get the license`() = runTest {
        //Given
        coEvery {
            licenseRepository.getLicenseStates(any(), any(), any())
        } returns listOf(
            LicenseState.Started,
            LicenseState.FinishedWithError("error")
        ).asFlow()

        // When
        sdkInitializer.initialize(null)
        // Then throws IllegalArgumentException
    }

    @Test(expected = BioSdkException.LicenseDownloadMaintenanceModeException::class)
    fun `test initialize failure with maintenance mode`() = runTest {
        //Given
        coEvery {
            licenseRepository.getLicenseStates(any(), any(), any())
        } returns listOf(
            LicenseState.Started,
            LicenseState.FinishedWithBackendMaintenanceError(null)
        ).asFlow()
        // When
        sdkInitializer.initialize(null)
        // Then throws
    }
}
