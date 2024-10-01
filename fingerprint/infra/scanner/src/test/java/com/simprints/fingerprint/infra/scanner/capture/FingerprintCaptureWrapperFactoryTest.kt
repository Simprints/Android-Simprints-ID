package com.simprints.fingerprint.infra.scanner.capture

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.NullScannerException
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test

class FingerprintCaptureWrapperFactoryTest {

    private lateinit var fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory


    @Before
    fun setUp() {
        fingerprintCaptureWrapperFactory =
            FingerprintCaptureWrapperFactory(
                UnconfinedTestDispatcher(), ScannerUiHelper(), mockk(relaxed = true)
            )
    }

    @Test(expected = NullScannerException::class)
    fun testThrowsNullScannerErrorWhenNotInitialized() {
        fingerprintCaptureWrapperFactory.captureWrapper
    }

    @Test
    fun testCreateV1() {
        fingerprintCaptureWrapperFactory.createV1(mockk())
        val wrapper = fingerprintCaptureWrapperFactory.captureWrapper
        Truth.assertThat(wrapper).isInstanceOf(FingerprintCaptureWrapperV1::class.java)
    }

    @Test
    fun testCreateV2() {
        fingerprintCaptureWrapperFactory.createV2(mockk())
        val wrapper = fingerprintCaptureWrapperFactory.captureWrapper
        Truth.assertThat(wrapper).isInstanceOf(FingerprintCaptureWrapperV2::class.java)
    }
}
