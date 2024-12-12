package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.models.NecImage
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ExtractNecTemplateUseCaseTest {
    @RelaxedMockK
    private lateinit var nec: NEC
    private lateinit var extractNecTemplateUseCase: ExtractNecTemplateUseCase

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        extractNecTemplateUseCase = ExtractNecTemplateUseCase(nec, testCoroutineRule.testCoroutineDispatcher)
    }

    @Test
    fun `test nec template extractor success`() = runTest {
        // Given
        val fingerprintImage = FingerprintImage(
            width = 500,
            height = 500,
            resolution = 500,
            imageBytes = ByteArray(0),
        )
        // When
        val result = extractNecTemplateUseCase(fingerprintImage, 100)
        // Then
        verify {
            nec.extract(
                NecImage(
                    width = fingerprintImage.width,
                    height = fingerprintImage.height,
                    resolution = fingerprintImage.resolution,
                    imageBytes = fingerprintImage.imageBytes,
                ),
            )
        }
        Truth.assertThat(result.templateMetadata?.imageQualityScore).isEqualTo(100)
    }

    @Test(expected = BioSdkException.TemplateExtractionException::class)
    fun `test nec template extractor failure`() = runTest {
        // Given
        val fingerprintImage = FingerprintImage(
            width = 500,
            height = 500,
            resolution = 500,
            imageBytes = ByteArray(0),
        )
        every { nec.extract(any()) } throws Exception()
        // When
        extractNecTemplateUseCase(fingerprintImage, 100)
        // Then throw exception
    }
}
