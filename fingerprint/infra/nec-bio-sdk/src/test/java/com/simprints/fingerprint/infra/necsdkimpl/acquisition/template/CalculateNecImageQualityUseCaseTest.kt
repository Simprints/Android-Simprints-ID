package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.fingerprint.FingerprintImageQualityCheck
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CalculateNecImageQualityUseCaseTest {
    companion object {
        private const val DEFAULT_GOOD_IMAGE_QUALITY = 87
    }

    @MockK
    private lateinit var nec: NEC
    private val testImage = FingerprintImage(byteArrayOf(), 1, 1, 1)
    private lateinit var calculateNecImageQualityUseCase: CalculateNecImageQualityUseCase

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        calculateNecImageQualityUseCase =
            CalculateNecImageQualityUseCase(nec, testCoroutineRule.testCoroutineDispatcher)
    }

    @Test
    fun `test getNECQualityCheckResult`() = runTest {
        // Given
        every { nec.qualityCheck(any()) } returns DEFAULT_GOOD_IMAGE_QUALITY

        // When
        val result = calculateNecImageQualityUseCase(testImage)
        // Then
        Truth.assertThat(result).isEqualTo(DEFAULT_GOOD_IMAGE_QUALITY)

    }

    @Test(expected = BioSdkException.ImageQualityCheckingException::class)
    fun `test isBadScan failure should throw`() = runTest() {
        every {
            nec.qualityCheck(any())
        } throws FingerprintImageQualityCheck.QualityCheckFailedException(-1)
        // When
        calculateNecImageQualityUseCase(testImage)
    }
}
