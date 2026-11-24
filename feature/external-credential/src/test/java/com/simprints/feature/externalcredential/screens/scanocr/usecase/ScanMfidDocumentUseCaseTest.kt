package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ReadoutResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ReadoutSource
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.infra.config.store.models.FaydaCardConfig
import com.simprints.infra.config.store.models.GhanaIdCardConfig
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.config.store.models.NhisCardConfig
import com.simprints.infra.credential.store.CredentialImageRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class ScanMfidDocumentUseCaseTest {
    @MockK
    private lateinit var readBarcode: ReadBarcodeUseCase

    @MockK
    private lateinit var readOcrText: ReadOcrTextUseCase

    @MockK
    private lateinit var credentialImageRepository: CredentialImageRepository

    private lateinit var useCase: ScanMfidDocumentUseCase

    private val bitmap = mockk<Bitmap>(relaxed = true)
    private val savedImagePath = "path/to/image.jpg"
    private val nhisConfig = MultiFactorIdConfiguration(
        allowedExternalCredentials = emptyList(),
        ghanaIdCardConfig = null,
        nhisCardConfig = NhisCardConfig(isCapturingAllFields = false),
        qrCodeConfig = null,
        faydaCardConfig = null,
    )
    private val ghanaIdConfig = MultiFactorIdConfiguration(
        allowedExternalCredentials = emptyList(),
        ghanaIdCardConfig = GhanaIdCardConfig(isCapturingAllFields = false),
        nhisCardConfig = null,
        qrCodeConfig = null,
        faydaCardConfig = null,
    )
    private val faydaConfig = MultiFactorIdConfiguration(
        allowedExternalCredentials = emptyList(),
        ghanaIdCardConfig = null,
        nhisCardConfig = null,
        qrCodeConfig = null,
        faydaCardConfig = FaydaCardConfig(isCapturingAllFields = false),
    )
    private val allFieldsConfig = MultiFactorIdConfiguration(
        allowedExternalCredentials = emptyList(),
        ghanaIdCardConfig = GhanaIdCardConfig(isCapturingAllFields = true),
        nhisCardConfig = NhisCardConfig(isCapturingAllFields = true),
        qrCodeConfig = null,
        faydaCardConfig = FaydaCardConfig(isCapturingAllFields = true),
    )
    private val nhisReadout = ReadoutResult(ReadoutSource.TEXT_OCR, OcrScanResult.GhanaNhisCard(credential = ocrLine()))
    private val faydaOcrReadout = ReadoutResult(ReadoutSource.TEXT_OCR, OcrScanResult.FaydaCard(credential = ocrLine("1234567812345678")))
    private val faydaBarcodeReadout = ReadoutResult(ReadoutSource.BARCODE, OcrScanResult.FaydaCard(credential = ocrLine("1234567812345678")))

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = ScanMfidDocumentUseCase(
            readBarcode = readBarcode,
            readOcrText = readOcrText,
            credentialImageRepository = credentialImageRepository,
        )
        coEvery { credentialImageRepository.saveCredentialScan(any(), any()) } returns savedImagePath
        every { readBarcode(any(), any()) } returns null
        every { readOcrText(any(), any(), any()) } returns null
    }

    @Test
    fun `returns null when nothing is read`() = runTest {
        val result = useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)

        assertThat(result).isNull()
        coVerify(exactly = 0) { credentialImageRepository.saveCredentialScan(any(), any()) }
    }

    @Test
    fun `returns saved document with readout source when text is read`() = runTest {
        every { readOcrText(bitmap, OcrDocumentType.NhisCard, false) } returns nhisReadout

        val result = useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)

        assertThat(result?.imagePath).isEqualTo(savedImagePath)
        assertThat(result?.ocrScanResult).isEqualTo(nhisReadout.scanResult)
        assertThat(result?.source).isEqualTo(ReadoutSource.TEXT_OCR)
        coVerify(exactly = 1) { credentialImageRepository.saveCredentialScan(bitmap, any()) }
    }

    @Test
    fun `returns FaydaCard from barcode without reading text`() = runTest {
        every { readBarcode(bitmap, OcrDocumentType.FaydaCard) } returns faydaBarcodeReadout

        val result = useCase(bitmap, OcrDocumentType.FaydaCard, faydaConfig, readFaydaBarcode = true)

        assertThat(result?.ocrScanResult).isEqualTo(faydaBarcodeReadout.scanResult)
        assertThat(result?.source).isEqualTo(ReadoutSource.BARCODE)
        verify(exactly = 0) { readOcrText(any(), any(), any()) }
    }

    @Test
    fun `falls back to text for FaydaCard when barcode is not read`() = runTest {
        every { readOcrText(bitmap, OcrDocumentType.FaydaCard, false) } returns faydaOcrReadout

        val result = useCase(bitmap, OcrDocumentType.FaydaCard, faydaConfig, readFaydaBarcode = true)

        assertThat(result?.ocrScanResult).isEqualTo(faydaOcrReadout.scanResult)
        assertThat(result?.source).isEqualTo(ReadoutSource.TEXT_OCR)
        verify(exactly = 1) { readBarcode(bitmap, OcrDocumentType.FaydaCard) }
    }

    @Test
    fun `passes the document type to the barcode reader`() = runTest {
        useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig, readFaydaBarcode = true)
        useCase(bitmap, OcrDocumentType.GhanaIdCard, ghanaIdConfig, readFaydaBarcode = true)

        verify(exactly = 1) { readBarcode(bitmap, OcrDocumentType.NhisCard) }
        verify(exactly = 1) { readBarcode(bitmap, OcrDocumentType.GhanaIdCard) }
    }

    @Test
    fun `does not read barcodes for FaydaCard when disabled`() = runTest {
        useCase(bitmap, OcrDocumentType.FaydaCard, faydaConfig, readFaydaBarcode = false)

        verify(exactly = 0) { readBarcode(any(), any()) }
        verify(exactly = 1) { readOcrText(bitmap, OcrDocumentType.FaydaCard, false) }
    }

    @Test
    fun `passes isCapturingAllFields from the matching card config`() = runTest {
        useCase(bitmap, OcrDocumentType.NhisCard, allFieldsConfig)
        useCase(bitmap, OcrDocumentType.GhanaIdCard, allFieldsConfig)
        useCase(bitmap, OcrDocumentType.FaydaCard, allFieldsConfig)

        verify { readOcrText(bitmap, OcrDocumentType.NhisCard, true) }
        verify { readOcrText(bitmap, OcrDocumentType.GhanaIdCard, true) }
        verify { readOcrText(bitmap, OcrDocumentType.FaydaCard, true) }
    }

    @Test
    fun `passes false for isCapturingAllFields when card config is absent`() = runTest {
        useCase(bitmap, OcrDocumentType.NhisCard, ghanaIdConfig)

        verify { readOcrText(bitmap, OcrDocumentType.NhisCard, false) }
    }

    @Test
    fun `returns null when exception is thrown`() = runTest {
        every { readOcrText(any(), any(), any()) } throws RuntimeException("OCR failed")

        val result = useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)

        assertThat(result).isNull()
    }

    private fun ocrLine(text: String = "12345678") = OcrLine(
        id = 0,
        text = text,
        boundingBox = BoundingBox(left = 0, top = 100, right = 200, bottom = 130),
        blockBoundingBox = BoundingBox(left = 0, top = 90, right = 200, bottom = 140),
        confidence = 1f,
    )
}
