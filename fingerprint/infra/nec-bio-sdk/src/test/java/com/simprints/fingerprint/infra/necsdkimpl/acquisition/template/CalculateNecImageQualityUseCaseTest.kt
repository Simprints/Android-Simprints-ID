package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.fingerprint.FingerprintImageQualityCheck
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class CalculateNecImageQualityUseCaseTest {
    companion object {
        private const val DEFAULT_GOOD_IMAGE_QUALITY = 87
    }

    @MockK
    private lateinit var nec: NEC
    private val testImage = FingerprintImage(byteArrayOf(), 1, 1, 1)
    private lateinit var calculateNecImageQualityUseCase: CalculateNecImageQualityUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        calculateNecImageQualityUseCase = CalculateNecImageQualityUseCase(nec)
    }

    @Test
    fun `test getNECQualityCheckResult`() {
        // Given
        every { nec.qualityCheck(any()) } returns DEFAULT_GOOD_IMAGE_QUALITY

        // When
        val result = calculateNecImageQualityUseCase(testImage)
        // Then
        Truth.assertThat(result).isEqualTo(DEFAULT_GOOD_IMAGE_QUALITY)

    }

    @Test(expected = BioSdkException.ImageQualityCheckingException::class)
    fun `test isBadScan failure should throw`() {
        every {
            nec.qualityCheck(any())
        } throws FingerprintImageQualityCheck.QualityCheckFailedException(-1)
        // When
        calculateNecImageQualityUseCase(testImage)
    }


}
