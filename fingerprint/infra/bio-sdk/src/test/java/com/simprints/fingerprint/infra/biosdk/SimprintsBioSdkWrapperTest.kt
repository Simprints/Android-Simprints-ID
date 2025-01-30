package com.simprints.fingerprint.infra.biosdk

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.basebiosdk.FingerprintBioSdk
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.ImageResponse
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateAcquisitionSettings
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateMetadata
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcherSettings
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SimprintsBioSdkWrapperTest {
    private lateinit var simprintsBioSdkWrapper: SimprintsBioSdkWrapper

    @MockK(relaxed = true)
    private lateinit var bioSdk:
        FingerprintBioSdk<Unit, Unit, Unit, FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata, SimAfisMatcherSettings>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        simprintsBioSdkWrapper = SimprintsBioSdkWrapper(bioSdk)
    }

    @Test
    fun `test Fixed Properties`() {
        // Given
        val expectedScanningTimeoutMs = 3000L
        val expectedImageTransferTimeoutMs = 3000L
        val expectedMinGoodScans = 2
        val expectedAddNewFingerOnBadScan = true

        // When
        val actualScanningTimeoutMs = simprintsBioSdkWrapper.scanningTimeoutMs
        val actualImageTransferTimeoutMs = simprintsBioSdkWrapper.imageTransferTimeoutMs
        val actualMinGoodScans = simprintsBioSdkWrapper.minGoodScans
        val actualAddNewFingerOnBadScan = simprintsBioSdkWrapper.addNewFingerOnBadScan

        // Then
        assertThat(actualScanningTimeoutMs).isEqualTo(expectedScanningTimeoutMs)
        assertThat(actualImageTransferTimeoutMs).isEqualTo(expectedImageTransferTimeoutMs)
        assertThat(actualMinGoodScans).isEqualTo(expectedMinGoodScans)
        assertThat(actualAddNewFingerOnBadScan).isEqualTo(expectedAddNewFingerOnBadScan)
    }

    @Test
    fun `Initializes bio sdk`() = runTest {
        // When
        simprintsBioSdkWrapper.initialize()
        // Then
        coVerify {
            bioSdk.initialize()
        }
    }

    @Test
    fun `Calls match on bio sdk`() = runTest {
        // Given
        val probe = mockk<FingerprintIdentity>()
        val candidates = listOf(mockk<FingerprintIdentity>())
        val isCrossFingerMatchingEnabled = true
        val settings = SimAfisMatcherSettings(isCrossFingerMatchingEnabled)
        // When
        simprintsBioSdkWrapper.match(probe, candidates, isCrossFingerMatchingEnabled)

        // Then
        coVerify { bioSdk.match(probe, candidates, settings) }
    }

    @Test
    fun `Calls fingerprint template acquisition from sdk`() = runTest {
        // Given
        val captureFingerprintStrategy = 1000
        val captureTimeOutMs = 1000
        val captureQualityThreshold = 100
        val captureAllowLowQualityExtraction = true

        val bioSdkResponse = TemplateResponse(
            byteArrayOf(1, 2, 3),
            FingerprintTemplateMetadata(
                "TemplateFormat",
                100,
            ),
        )
        val settingsSlot = slot<FingerprintTemplateAcquisitionSettings>()
        coEvery { bioSdk.acquireFingerprintTemplate(capture(settingsSlot)) } returns bioSdkResponse

        // When
        val response = simprintsBioSdkWrapper.acquireFingerprintTemplate(
            captureFingerprintStrategy,
            captureTimeOutMs,
            captureQualityThreshold,
            captureAllowLowQualityExtraction,
        )

        // Then
        coVerify { bioSdk.acquireFingerprintTemplate(any()) }
        with(settingsSlot.captured) {
            assertThat(captureFingerprintDpi?.value).isEqualTo(captureFingerprintStrategy.toShort())
            assertThat(timeOutMs).isEqualTo(captureTimeOutMs)
            assertThat(qualityThreshold).isEqualTo(captureQualityThreshold)
            assertThat(allowLowQualityExtraction).isEqualTo(captureAllowLowQualityExtraction)
        }
        assertThat(bioSdkResponse.template).isEqualTo(response.template)
        assertThat(bioSdkResponse.templateMetadata?.templateFormat).isEqualTo(response.templateFormat)
        assertThat(bioSdkResponse.templateMetadata?.imageQualityScore).isEqualTo(response.imageQualityScore)
    }

    @Test
    fun `Fails if template does not have meta data`() = runTest {
        coEvery { bioSdk.acquireFingerprintTemplate(any()) } returns TemplateResponse(
            byteArrayOf(
                1,
                2,
                3,
            ),
            null,
        )

        assertThrows<IllegalArgumentException> {
            simprintsBioSdkWrapper.acquireFingerprintTemplate(1, 1, 1, true)
        }
    }

    @Test
    fun `Calls fingerprint image acquisition from sdk`() = runTest {
        // Given
        val imageBytes = byteArrayOf(1, 2, 3)
        val bioSdkResponse = ImageResponse<Unit>(imageBytes)
        coEvery { bioSdk.acquireFingerprintImage() } returns bioSdkResponse

        // When
        val response = simprintsBioSdkWrapper.acquireFingerprintImage()
        // Then
        coVerify { bioSdk.acquireFingerprintImage() }
        assertThat(bioSdkResponse.imageBytes).isEqualTo(response.imageBytes)
    }
}
