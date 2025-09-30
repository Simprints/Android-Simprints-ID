package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.content.Context
import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

internal class SaveScannedImageUseCaseTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var bitmap: Bitmap

    private lateinit var useCase: SaveScannedImageUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        val tempDir = createTempDir("test_cache")
        every { context.cacheDir } returns tempDir
        useCase = SaveScannedImageUseCase(context)
    }

    @Test
    fun `saves bitmap with correct document type name and returns file path`() {
        OcrDocumentType.entries.forEach { documentType ->
            every { bitmap.compress(any(), any(), any()) } returns true
            val result = useCase(bitmap, documentType)
            assertThat(result).contains("ocr_scan_${documentType}_")
            assertThat(result).endsWith(".jpg")
            verify { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, any()) }
        }
    }
}
