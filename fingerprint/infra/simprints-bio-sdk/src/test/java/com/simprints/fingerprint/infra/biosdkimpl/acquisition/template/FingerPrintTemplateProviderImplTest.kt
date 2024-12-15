package com.simprints.fingerprint.infra.biosdkimpl.acquisition.template

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FingerPrintTemplateProviderImplTest {
    private lateinit var fingerprintTemplateProviderImpl: FingerprintTemplateProviderImpl

    @MockK
    private lateinit var fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fingerprintTemplateProviderImpl =
            FingerprintTemplateProviderImpl(fingerprintCaptureWrapperFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun acquireFingerprintTemplateThrowsIfSettingsIsNull() = runTest {
        val response = fingerprintTemplateProviderImpl.acquireFingerprintTemplate(null)
    }

    @Test
    fun acquireFingerprintTemplate() = runTest {
        // Given
        val settings = FingerprintTemplateAcquisitionSettings(
            Dpi(1000),
            1000,
            100,
        )
        val templateResponse = AcquireFingerprintTemplateResponse(
            byteArrayOf(),
            "templateFormat",
            100,
        )
        coEvery {
            fingerprintCaptureWrapperFactory.captureWrapper.acquireFingerprintTemplate(
                settings.captureFingerprintDpi,
                settings.timeOutMs,
                settings.qualityThreshold,
                settings.allowLowQualityExtraction,
            )
        } returns templateResponse
        // When
        val response = fingerprintTemplateProviderImpl.acquireFingerprintTemplate(settings)
        // Then
        Truth.assertThat(response.template).isEqualTo(templateResponse.template)
        Truth
            .assertThat(response.templateMetadata?.templateFormat)
            .isEqualTo(templateResponse.templateFormat)
        Truth
            .assertThat(response.templateMetadata?.imageQualityScore)
            .isEqualTo(templateResponse.imageQualityScore)
    }
}
