package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.google.common.truth.Truth
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.fingerprint.FingerprintImageQualityCheck
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class NecImageQualityCalculatorTest {
    companion object {
        private const val DEFAULT_GOOD_IMAGE_QUALITY = 87
    }

    @MockK
    private lateinit var nec: NEC
    private val testImage = FingerprintImage(byteArrayOf(), 1, 1, 1)
    private lateinit var imageQualityChecker: NecImageQualityCalculator

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        imageQualityChecker = NecImageQualityCalculator(nec)
    }

    @Test
    fun `test getNECQualityCheckResult`() {
        // Given
        every { nec.qualityCheck(any()) } returns DEFAULT_GOOD_IMAGE_QUALITY

        // When
        val result = imageQualityChecker.getQualityScore(testImage)
        // Then
        Truth.assertThat(result).isEqualTo(DEFAULT_GOOD_IMAGE_QUALITY)

    }

    @Test(expected = FingerprintImageQualityCheck.QualityCheckFailedException::class)
    fun `test isBadScan failure should throw`() {
        every {
            nec.qualityCheck(any())
        } throws FingerprintImageQualityCheck.QualityCheckFailedException(-1)
        // When
        imageQualityChecker.getQualityScore(testImage)
    }


}
