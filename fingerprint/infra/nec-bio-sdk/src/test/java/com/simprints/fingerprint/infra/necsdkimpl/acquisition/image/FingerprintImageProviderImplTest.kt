package com.simprints.fingerprint.infra.necsdkimpl.acquisition.image

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FingerprintImageProviderImplTest {
    private lateinit var fingerprintImageProvider: FingerprintImageProviderImpl
    private val imageCache = ProcessedImageCache()

    @Before
    fun setUp() {
        fingerprintImageProvider = FingerprintImageProviderImpl(imageCache)
    }

    @Test
    fun `test acquireFingerprintImage success`() = runTest {
        // Given
        imageCache.recentlyCapturedImage = byteArrayOf(1, 2, 3)
        // When
        val result = fingerprintImageProvider.acquireFingerprintImage(null)
        // Then
        Truth.assertThat(result.imageBytes).isEqualTo(imageCache.recentlyCapturedImage)
    }

    @Test(expected = BioSdkException.CannotAcquireFingerprintImageException::class)
    fun `test acquireFingerprintImage failure`() = runTest {
        // Given
        imageCache.recentlyCapturedImage = null
        // When
        fingerprintImageProvider.acquireFingerprintImage(null)
        // Then throw exception
    }
}
