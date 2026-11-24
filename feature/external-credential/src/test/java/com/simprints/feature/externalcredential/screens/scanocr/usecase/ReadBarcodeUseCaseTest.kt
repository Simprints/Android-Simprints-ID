package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ReadoutSource
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.scanocr.reader.ScannedBarcode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class ReadBarcodeUseCaseTest {
    @MockK
    private lateinit var readBarcodesFromImage: ReadBarcodesFromImageUseCase

    @MockK
    private lateinit var faydaCardOcrReaderUseCase: FaydaCardOcrReaderUseCase

    private lateinit var useCase: ReadBarcodeUseCase

    private val bitmap = mockk<Bitmap>(relaxed = true)
    private val barcodes = listOf(ScannedBarcode(value = "1234567812345678", boundingBox = BoundingBox(0, 0, 10, 10)))

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = ReadBarcodeUseCase(
            readBarcodesFromImage = readBarcodesFromImage,
            faydaCardOcrReaderUseCase = faydaCardOcrReaderUseCase,
        )
        every { readBarcodesFromImage(bitmap) } returns barcodes
    }

    @Test
    fun `returns null when no Fayda credential is found in barcodes`() {
        every { faydaCardOcrReaderUseCase.fromBarcode(barcodes) } returns null

        assertThat(useCase(bitmap, OcrDocumentType.FaydaCard)).isNull()
    }

    @Test
    fun `returns BARCODE readout when Fayda credential is found`() {
        val scanResult = OcrScanResult.FaydaCard(
            credential = OcrLine(
                id = 0,
                text = "1234567812345678",
                boundingBox = BoundingBox(left = 0, top = 0, right = 10, bottom = 10),
                blockBoundingBox = BoundingBox(left = 0, top = 0, right = 10, bottom = 10),
                confidence = 1f,
            ),
        )
        every { faydaCardOcrReaderUseCase.fromBarcode(barcodes) } returns scanResult

        val result = useCase(bitmap, OcrDocumentType.FaydaCard)

        assertThat(result?.source).isEqualTo(ReadoutSource.BARCODE)
        assertThat(result?.scanResult).isEqualTo(scanResult)
    }

    @Test
    fun `returns null for Ghana cards without reading barcodes`() {
        assertThat(useCase(bitmap, OcrDocumentType.NhisCard)).isNull()
        assertThat(useCase(bitmap, OcrDocumentType.GhanaIdCard)).isNull()

        verify(exactly = 0) { readBarcodesFromImage(any()) }
        verify(exactly = 0) { faydaCardOcrReaderUseCase.fromBarcode(any()) }
    }
}
