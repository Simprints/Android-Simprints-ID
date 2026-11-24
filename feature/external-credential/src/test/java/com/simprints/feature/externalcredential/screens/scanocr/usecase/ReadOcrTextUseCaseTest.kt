package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ReadoutSource
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class ReadOcrTextUseCaseTest {
    @MockK
    private lateinit var readTextFromImage: ReadTextFromImageUseCase

    @MockK
    private lateinit var ghanaNhisCardOcrReaderUseCase: GhanaNhisCardOcrReaderUseCase

    @MockK
    private lateinit var ghanaIdCardOcrReaderUseCase: GhanaIdCardOcrReaderUseCase

    @MockK
    private lateinit var faydaCardOcrReaderUseCase: FaydaCardOcrReaderUseCase

    private lateinit var useCase: ReadOcrTextUseCase

    private val bitmap = mockk<Bitmap>(relaxed = true)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = ReadOcrTextUseCase(
            readTextFromImage = readTextFromImage,
            ghanaNhisCardOcrReaderUseCase = ghanaNhisCardOcrReaderUseCase,
            ghanaIdCardOcrReaderUseCase = ghanaIdCardOcrReaderUseCase,
            faydaCardOcrReaderUseCase = faydaCardOcrReaderUseCase,
        )
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrReaderUseCase(any(), any()) } returns null
        every { ghanaIdCardOcrReaderUseCase(any(), any()) } returns null
        every { faydaCardOcrReaderUseCase.fromText(any()) } returns null
    }

    @Test
    fun `returns null when no text is read`() {
        every { readTextFromImage(bitmap) } returns null

        assertThat(useCase(bitmap, OcrDocumentType.NhisCard, false)).isNull()
        verify(exactly = 0) { ghanaNhisCardOcrReaderUseCase(any(), any()) }
    }

    @Test
    fun `returns null when reader finds no credential`() {
        assertThat(useCase(bitmap, OcrDocumentType.NhisCard, false)).isNull()
        assertThat(useCase(bitmap, OcrDocumentType.GhanaIdCard, false)).isNull()
        assertThat(useCase(bitmap, OcrDocumentType.FaydaCard, false)).isNull()
    }

    @Test
    fun `returns TEXT_OCR readout for NhisCard`() {
        val scanResult = OcrScanResult.GhanaNhisCard(credential = ocrLine("12345678"))
        every { ghanaNhisCardOcrReaderUseCase(any(), any()) } returns scanResult

        val result = useCase(bitmap, OcrDocumentType.NhisCard, false)

        assertThat(result?.source).isEqualTo(ReadoutSource.TEXT_OCR)
        assertThat(result?.scanResult).isEqualTo(scanResult)
    }

    @Test
    fun `returns TEXT_OCR readout for GhanaIdCard`() {
        val scanResult = OcrScanResult.GhanaIdCard(credential = ocrLine("GHA-123456789-0"))
        every { ghanaIdCardOcrReaderUseCase(any(), any()) } returns scanResult

        val result = useCase(bitmap, OcrDocumentType.GhanaIdCard, false)

        assertThat(result?.source).isEqualTo(ReadoutSource.TEXT_OCR)
        assertThat(result?.scanResult).isEqualTo(scanResult)
    }

    @Test
    fun `returns TEXT_OCR readout for FaydaCard`() {
        val scanResult = OcrScanResult.FaydaCard(credential = ocrLine("1234567812345678"))
        every { faydaCardOcrReaderUseCase.fromText(any()) } returns scanResult

        val result = useCase(bitmap, OcrDocumentType.FaydaCard, false)

        assertThat(result?.source).isEqualTo(ReadoutSource.TEXT_OCR)
        assertThat(result?.scanResult).isEqualTo(scanResult)
    }

    @Test
    fun `delegates to the reader of the document type only`() {
        useCase(bitmap, OcrDocumentType.NhisCard, false)
        verify(exactly = 1) { ghanaNhisCardOcrReaderUseCase(any(), any()) }
        verify(exactly = 0) { ghanaIdCardOcrReaderUseCase(any(), any()) }
        verify(exactly = 0) { faydaCardOcrReaderUseCase.fromText(any()) }

        useCase(bitmap, OcrDocumentType.GhanaIdCard, false)
        verify(exactly = 1) { ghanaIdCardOcrReaderUseCase(any(), any()) }

        useCase(bitmap, OcrDocumentType.FaydaCard, false)
        verify(exactly = 1) { faydaCardOcrReaderUseCase.fromText(any()) }
    }

    @Test
    fun `passes isCapturingAllFields to Ghana readers`() {
        useCase(bitmap, OcrDocumentType.NhisCard, true)
        useCase(bitmap, OcrDocumentType.GhanaIdCard, true)

        verify { ghanaNhisCardOcrReaderUseCase(any(), isCapturingAllFields = true) }
        verify { ghanaIdCardOcrReaderUseCase(any(), isCapturingAllFields = true) }
    }

    private fun ocrLine(text: String) = OcrLine(
        id = 0,
        text = text,
        boundingBox = BoundingBox(left = 0, top = 100, right = 200, bottom = 130),
        blockBoundingBox = BoundingBox(left = 0, top = 90, right = 200, bottom = 140),
        confidence = 1f,
    )
}
