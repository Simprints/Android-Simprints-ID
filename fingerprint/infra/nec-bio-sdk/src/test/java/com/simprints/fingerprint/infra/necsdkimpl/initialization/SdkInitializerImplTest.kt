package com.simprints.fingerprint.infra.necsdkimpl.initialization

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseState
import com.simprints.necwrapper.nec.NEC
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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

    @MockK
    lateinit var authStore: AuthStore

    lateinit var licesneResponseList: List<LicenseState>

    private lateinit var sdkInitializer: SdkInitializer<Unit>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        justRun { nec.init(any(), context) }

        every { authStore.signedInProjectId } returns PROJECT_ID
        every {
            licenseRepository.getLicenseStates(
                DEVICE_ID,
                PROJECT_ID,
                SdkInitializerImpl.NEC_VENDOR
            )
        } returns licesneResponseList.asFlow()

        sdkInitializer =
            SdkInitializerImpl(context, DEVICE_ID, nec, licenseRepository, authStore)
    }

    @Test
    fun `test initialize success`() = runTest {
        //Given
        val license = "license"
        licesneResponseList = listOf(LicenseState.FinishedWithSuccess(license))


        // When
        sdkInitializer.initialize(null)
        // Then
        verify { nec.init(any(), context) }


    }

    @Test(expected = IllegalArgumentException::class)
    fun `test initialize failure`() = runTest {
        //Given
        val license = null
        licesneResponseList = listOf(LicenseState.FinishedWithError("error"))
        // When
        sdkInitializer.initialize(license)
        // Then throws IllegalArgumentException
    }

    companion object {
        private const val DEVICE_ID = "deviceId"
        private const val PROJECT_ID = "projectId"

    }
}
