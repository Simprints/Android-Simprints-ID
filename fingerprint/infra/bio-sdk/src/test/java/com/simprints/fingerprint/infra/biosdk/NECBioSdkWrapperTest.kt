package com.simprints.fingerprint.infra.biosdk

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.basebiosdk.FingerprintBioSdk
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.ImageResponse
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.FingerprintTemplateAcquisitionSettings
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.FingerprintTemplateMetadata
import com.simprints.fingerprint.infra.necsdkimpl.matching.NecMatchingSettings
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NECBioSdkWrapperTest {
    private lateinit var necBioSdkWrapper: NECBioSdkWrapper

    @RelaxedMockK
    private lateinit var bioSdk: FingerprintBioSdk<
        Unit,
        Unit,
        Unit,
        FingerprintTemplateAcquisitionSettings,
        FingerprintTemplateMetadata,
        NecMatchingSettings,
    >

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        necBioSdkWrapper = NECBioSdkWrapper(bioSdk)
    }

    @Test
    fun `test Fixed Properties`() {
        // Given
        val expectedScanningTimeoutMs = 8000L
        val expectedImageTransferTimeoutMs = 0L
        val expectedMinGoodScans = 0
        val expectedAddNewFingerOnBadScan = false

        // When
        val actualScanningTimeoutMs = necBioSdkWrapper.scanningTimeoutMs
        val actualImageTransferTimeoutMs = necBioSdkWrapper.imageTransferTimeoutMs
        val actualMinGoodScans = necBioSdkWrapper.minGoodScans
        val actualAddNewFingerOnBadScan = necBioSdkWrapper.addNewFingerOnBadScan

        // Then
        assertThat(actualScanningTimeoutMs).isEqualTo(expectedScanningTimeoutMs)
        assertThat(actualImageTransferTimeoutMs).isEqualTo(expectedImageTransferTimeoutMs)
        assertThat(actualMinGoodScans).isEqualTo(expectedMinGoodScans)
        assertThat(actualAddNewFingerOnBadScan).isEqualTo(expectedAddNewFingerOnBadScan)
    }

    @Test
    fun `initializes bio sdk`() = runTest {
        // When
        necBioSdkWrapper.initialize()
        // Then
        coVerify {
            bioSdk.initialize()
        }
    }

    @Test
    fun `calls match on bio sdk`() = runTest {
        // Given
        val probe = mockk<FingerprintIdentity>()
        val candidates = listOf(mockk<FingerprintIdentity>())
        val isCrossFingerMatchingEnabled = true
        val settings = NecMatchingSettings(isCrossFingerMatchingEnabled)
        // When
        necBioSdkWrapper.match(probe, candidates, isCrossFingerMatchingEnabled)

        // Then
        coVerify { bioSdk.match(probe, candidates, settings) }
    }

    @Test
    fun `calls fingerprint template acquisition from sdk`() = runTest {
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
        val response = necBioSdkWrapper.acquireFingerprintTemplate(
            captureFingerprintStrategy,
            captureTimeOutMs,
            captureQualityThreshold,
            captureAllowLowQualityExtraction,
        )

        // Then
        coVerify { bioSdk.acquireFingerprintTemplate(any()) }
        with(settingsSlot.captured) {
            assertThat(processingResolution?.value)
                .isEqualTo(captureFingerprintStrategy.toShort())
            assertThat(timeOutMs).isEqualTo(captureTimeOutMs)
            assertThat(qualityThreshold).isEqualTo(captureQualityThreshold)
            assertThat(allowLowQualityExtraction).isEqualTo(captureAllowLowQualityExtraction)
        }
        assertThat(bioSdkResponse.template).isEqualTo(response.template)

        assertThat(bioSdkResponse.templateMetadata?.templateFormat)
            .isEqualTo(response.templateFormat)

        assertThat(bioSdkResponse.templateMetadata?.imageQualityScore)
            .isEqualTo(response.imageQualityScore)
    }

    @Test
    fun `fails if template does not have meta data`() = runTest {
        coEvery { bioSdk.acquireFingerprintTemplate(any()) } returns TemplateResponse(
            byteArrayOf(
                1,
                2,
                3,
            ),
            null,
        )

        assertThrows<IllegalArgumentException> {
            necBioSdkWrapper.acquireFingerprintTemplate(1, 1, 1, true)
        }
    }

    @Test
    fun `calls fingerprint image acquisition from sdk`() = runTest {
        // Given
        val imageBytes = byteArrayOf(1, 2, 3)
        val bioSdkResponse = ImageResponse<Unit>(imageBytes)
        coEvery { bioSdk.acquireFingerprintImage() } returns bioSdkResponse

        // When
        val response = necBioSdkWrapper.acquireFingerprintImage()
        // Then
        coVerify { bioSdk.acquireFingerprintImage() }
        assertThat(bioSdkResponse.imageBytes).isEqualTo(response.imageBytes)
    }
}
