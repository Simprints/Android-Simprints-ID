package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import android.content.SharedPreferences
import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapper
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.security.SecurityManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class AcquireImageDistortionConfigurationUseCaseTest {

    @MockK
    private lateinit var fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory

    @MockK
    private lateinit var captureWrapper: FingerprintCaptureWrapper

    @MockK
    private lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    private lateinit var securityManager: SecurityManager

    @RelaxedMockK
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var acquireImageDistortionConfigurationUseCase: AcquireImageDistortionConfigurationUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { fingerprintCaptureWrapperFactory.captureWrapper } returns captureWrapper
        every { securityManager.buildEncryptedSharedPreferences(any()) } returns sharedPreferences
        coEvery { recentUserActivityManager.getRecentUserActivity().lastScannerUsed } returns "scannerId"

        acquireImageDistortionConfigurationUseCase = AcquireImageDistortionConfigurationUseCase(
            fingerprintCaptureWrapperFactory, securityManager, recentUserActivityManager
        )

    }

    @Test
    fun `should acquire image distortion configuration from scanner if not available in shared preferences`() =
        runTest {

            // given
            val distortionConfiguration = byteArrayOf(1, 2, 3).toHexString()
            every { sharedPreferences.getString(any(),null) } returns null
            coEvery {
                captureWrapper.acquireImageDistortionMatrixConfiguration().configurationBytes
            } returns distortionConfiguration.hexToByteArray()


            // when
            val result = acquireImageDistortionConfigurationUseCase()

            // then
            Truth.assertThat(result.toHexString())
                .isEqualTo(distortionConfiguration)
            coVerify { captureWrapper.acquireImageDistortionMatrixConfiguration() }
            coVerify { sharedPreferences.edit().putString(any(), any())}
        }

    @Test
    fun `should acquire image distortion configuration from shared preferences if available`() =
        runTest {

            // given
            val distortionConfiguration = byteArrayOf(1, 2, 3).toHexString()
            every { sharedPreferences.getString(any(),null) } returns distortionConfiguration


            // when
            val result = acquireImageDistortionConfigurationUseCase()

            // then
            Truth.assertThat(result.toHexString())
                .isEqualTo(distortionConfiguration)
            coVerify(exactly = 0) { captureWrapper.acquireImageDistortionMatrixConfiguration() }
        }
}
