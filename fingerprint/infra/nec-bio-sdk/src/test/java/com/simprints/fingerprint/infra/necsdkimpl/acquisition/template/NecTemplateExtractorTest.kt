package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.models.NecImage
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class NecTemplateExtractorTest {

    @RelaxedMockK
    private lateinit var nec: NEC
    private lateinit var necTemplateExtractor: NecTemplateExtractor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        necTemplateExtractor = NecTemplateExtractor(nec)
    }
    @Test
    fun `test nec template extractor success`() {
        // Given
        val fingerprintImage = FingerprintImage(
            width = 500,
            height = 500,
            resolution = 500,
            imageBytes = ByteArray(0)
        )
        // When
       val result=necTemplateExtractor.extract(fingerprintImage, 100)
        // Then
        verify {
            nec.extract(
                NecImage(
                    width = fingerprintImage.width,
                    height = fingerprintImage.height,
                    resolution = fingerprintImage.resolution,
                    imageBytes = fingerprintImage.imageBytes
                )
            )
        }
        Truth.assertThat(result.templateMetadata?.imageQualityScore).isEqualTo(100)
    }
@Test(expected = BioSdkException.TemplateExtractionException::class)
    fun `test nec template extractor failure`() {
        // Given
        val fingerprintImage = FingerprintImage(
            width = 500,
            height = 500,
            resolution = 500,
            imageBytes = ByteArray(0)
        )
        every { nec.extract(any()) } throws Exception()
        // When
        necTemplateExtractor.extract(fingerprintImage, 100)
        // Then throw exception
    }

}
