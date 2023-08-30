package com.simprints.fingerprint.biosdk

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.data.domain.fingerprint.toDomain
import com.simprints.fingerprint.infra.basebiosdk.FingerprintBioSdk
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.ImageResponse
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.TemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateAcquisitionSettings
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateMetadata
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcherSettings
import com.simprints.infra.config.domain.models.Vero2Configuration
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
    private lateinit var bioSdk: FingerprintBioSdk<Unit, Unit, Unit, FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata, SimAfisMatcherSettings>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        simprintsBioSdkWrapper = SimprintsBioSdkWrapper(bioSdk)
    }

    @Test
    fun testInitialize() = runTest {
        //When
        simprintsBioSdkWrapper.initialize()
        //Then
        coVerify {
            bioSdk.initialize()
        }
    }

    @Test
    fun testMatch() = runTest {
        //Given
        val probe = mockk<FingerprintIdentity>()
        val candidates = listOf(mockk<FingerprintIdentity>())
        val isCrossFingerMatchingEnabled = true
        val settings = SimAfisMatcherSettings(isCrossFingerMatchingEnabled)
        //When
        simprintsBioSdkWrapper.match(probe, candidates, isCrossFingerMatchingEnabled)

        //Then
        coVerify { bioSdk.match(probe, candidates, settings) }
    }

    @Test
    fun testAcquireFingerprintTemplate() = runTest {
        //Given
        val captureFingerprintStrategy: Vero2Configuration.CaptureStrategy =
            Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI
        val captureTimeOutMs = 1000
        val captureQualityThreshold = 100

        val bioSdkResponse = TemplateResponse(
            byteArrayOf(1, 2, 3), FingerprintTemplateMetadata(
                "TemplateFormat", 100
            )
        )
        val settingsSlot = slot<FingerprintTemplateAcquisitionSettings>()
        coEvery { bioSdk.acquireFingerprintTemplate(capture(settingsSlot)) } returns bioSdkResponse

        //When
        val response = simprintsBioSdkWrapper.acquireFingerprintTemplate(
            captureFingerprintStrategy, captureTimeOutMs, captureQualityThreshold
        )

        //Then
        coVerify { bioSdk.acquireFingerprintTemplate(any()) }
        with(settingsSlot.captured) {
            assertThat(captureFingerprintDpi?.value).isEqualTo(captureFingerprintStrategy.toDomain().value)
            assertThat(timeOutMs).isEqualTo(captureTimeOutMs)
            assertThat(qualityThreshold).isEqualTo(captureQualityThreshold)
        }
        assertThat(bioSdkResponse.template).isEqualTo(response.template)
        assertThat(bioSdkResponse.templateMetadata?.templateFormat).isEqualTo(response.templateFormat)
        assertThat(bioSdkResponse.templateMetadata?.imageQualityScore).isEqualTo(response.imageQualityScore)
    }

    @Test
    fun testAcquireFingerprintImage() = runTest {
        //Given
        val imageBytes = byteArrayOf(1, 2, 3)
        val bioSdkResponse = ImageResponse<Unit>(imageBytes)
        coEvery { bioSdk.acquireFingerprintImage() } returns bioSdkResponse

        //When
        val response = simprintsBioSdkWrapper.acquireFingerprintImage()
        //Then
        coVerify { bioSdk.acquireFingerprintImage() }
        assertThat(bioSdkResponse.imageBytes).isEqualTo(response.imageBytes)


    }
}
