package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
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
    private lateinit var readTextFromImage: ReadTextFromImageUseCase

    @MockK
    private lateinit var ghanaNhisCardOcrReaderUseCase: GhanaNhisCardOcrReaderUseCase

    @MockK
    private lateinit var ghanaIdCardOcrReaderUseCase: GhanaIdCardOcrReaderUseCase

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
    )
    private val ghanaIdConfig = MultiFactorIdConfiguration(
        allowedExternalCredentials = emptyList(),
        ghanaIdCardConfig = GhanaIdCardConfig(isCapturingAllFields = false),
        nhisCardConfig = null,
        qrCodeConfig = null,
    )
    private val allFieldsConfig = MultiFactorIdConfiguration(
        allowedExternalCredentials = emptyList(),
        ghanaIdCardConfig = GhanaIdCardConfig(isCapturingAllFields = true),
        nhisCardConfig = NhisCardConfig(isCapturingAllFields = true),
        qrCodeConfig = null,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = ScanMfidDocumentUseCase(
            readTextFromImage = readTextFromImage,
            ghanaNhisCardOcrReaderUseCase = ghanaNhisCardOcrReaderUseCase,
            ghanaIdCardOcrReaderUseCase = ghanaIdCardOcrReaderUseCase,
            credentialImageRepository = credentialImageRepository,
        )
        coEvery { credentialImageRepository.saveCredentialScan(any(), any()) } returns savedImagePath
    }

    @Test
    fun `returns null when readTextFromImage returns null`() = runTest {
        every { readTextFromImage(bitmap) } returns null
        val result = useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)
        assertThat(result).isNull()
    }

    @Test
    fun `returns null when selector finds no matching line for NhisCard`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrReaderUseCase(any(), any()) } returns null
        val result = useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)
        assertThat(result).isNull()
    }

    @Test
    fun `returns null when selector finds no matching line for GhanaIdCard`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaIdCardOcrReaderUseCase(any(), any()) } returns null

        val result = useCase(bitmap, OcrDocumentType.GhanaIdCard, ghanaIdConfig)

        assertThat(result).isNull()
    }

    @Test
    fun `returns ScannedMfidDocument for NhisCard when line is found`() = runTest {
        val credentialLine = ocrLine(text = "12345678")
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrReaderUseCase(any(), any()) } returns OcrScanResult.GhanaNhisCard(credential = credentialLine)

        val result = useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)

        assertThat(result).isNotNull()
        assertThat(result?.ocrScanResult?.credential?.text).isEqualTo(credentialLine.text)
        assertThat(result?.ocrScanResult?.credential?.boundingBox).isEqualTo(credentialLine.boundingBox)
        assertThat(result?.ocrScanResult).isInstanceOf(OcrScanResult.GhanaNhisCard::class.java)
    }

    @Test
    fun `returns ScannedMfidDocument for GhanaIdCard when line is found`() = runTest {
        val credentialLine = ocrLine(text = "GHA-123456789-0")
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaIdCardOcrReaderUseCase(any(), any()) } returns OcrScanResult.GhanaIdCard(credential = credentialLine)

        val result = useCase(bitmap, OcrDocumentType.GhanaIdCard, ghanaIdConfig)

        assertThat(result).isNotNull()
        assertThat(result?.ocrScanResult?.credential?.text).isEqualTo(credentialLine.text)
        assertThat(result?.ocrScanResult).isInstanceOf(OcrScanResult.GhanaIdCard::class.java)
    }

    @Test
    fun `saves image when line is found`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrReaderUseCase(any(), any()) } returns OcrScanResult.GhanaNhisCard(credential = ocrLine())

        useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)

        coVerify(exactly = 1) { credentialImageRepository.saveCredentialScan(bitmap, any()) }
    }

    @Test
    fun `does not save image when no line is found`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrReaderUseCase(any(), any()) } returns null

        useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)

        coVerify(exactly = 0) { credentialImageRepository.saveCredentialScan(any(), any()) }
    }

    @Test
    fun `saved image path is set in result`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrReaderUseCase(any(), any()) } returns OcrScanResult.GhanaNhisCard(credential = ocrLine())

        val result = useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)

        assertThat(result?.imagePath).isEqualTo(savedImagePath)
    }

    @Test
    fun `returns null when exception is thrown`() = runTest {
        every { readTextFromImage(bitmap) } throws RuntimeException("OCR failed")
        val result = useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)
        assertThat(result).isNull()
    }

    @Test
    fun `delegates NhisCard to nhis selector`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)

        useCase(bitmap, OcrDocumentType.NhisCard, nhisConfig)

        verify(exactly = 1) { ghanaNhisCardOcrReaderUseCase(any(), any()) }
        verify(exactly = 0) { ghanaIdCardOcrReaderUseCase(any(), any()) }
    }

    @Test
    fun `delegates GhanaIdCard to ghana id selector`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)

        useCase(bitmap, OcrDocumentType.GhanaIdCard, ghanaIdConfig)

        verify(exactly = 1) { ghanaIdCardOcrReaderUseCase(any(), any()) }
        verify(exactly = 0) { ghanaNhisCardOcrReaderUseCase(any(), any()) }
    }

    @Test
    fun `passes isCapturingAllFields from nhis config to selector`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        useCase(bitmap, OcrDocumentType.NhisCard, allFieldsConfig)
        verify { ghanaNhisCardOcrReaderUseCase(any(), isCapturingAllFields = true) }
    }

    @Test
    fun `passes isCapturingAllFields from ghana id config to selector`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        useCase(bitmap, OcrDocumentType.GhanaIdCard, allFieldsConfig)
        verify { ghanaIdCardOcrReaderUseCase(any(), isCapturingAllFields = true) }
    }

    @Test
    fun `passes false for isCapturingAllFields when config is absent for nhis`() = runTest {
        val configWithoutNhis = MultiFactorIdConfiguration(
            allowedExternalCredentials = emptyList(),
            ghanaIdCardConfig = null,
            nhisCardConfig = null,
            qrCodeConfig = null,
        )
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)

        useCase(bitmap, OcrDocumentType.NhisCard, configWithoutNhis)

        verify { ghanaNhisCardOcrReaderUseCase(any(), isCapturingAllFields = false) }
    }

    private fun ocrLine(text: String = "12345678") = OcrLine(
        id = 0,
        text = text,
        boundingBox = BoundingBox(left = 0, top = 100, right = 200, bottom = 130),
        blockBoundingBox = BoundingBox(left = 0, top = 90, right = 200, bottom = 140),
        confidence = 1f,
    )
}
