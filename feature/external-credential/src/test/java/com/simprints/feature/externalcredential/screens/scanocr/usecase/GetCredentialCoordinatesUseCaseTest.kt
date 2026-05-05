package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.infra.credential.store.CredentialImageRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class GetCredentialCoordinatesUseCaseTest {
    @MockK
    private lateinit var readTextFromImage: ReadTextFromImageUseCase

    @MockK
    private lateinit var ghanaNhisCardOcrSelectorUseCase: GhanaNhisCardOcrSelectorUseCase

    @MockK
    private lateinit var ghanaIdCardOcrSelectorUseCase: GhanaIdCardOcrSelectorUseCase

    @MockK
    private lateinit var credentialImageRepository: CredentialImageRepository

    private lateinit var useCase: GetCredentialCoordinatesUseCase

    private val bitmap = mockk<Bitmap>(relaxed = true)
    private val savedImagePath = "path/to/image.jpg"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetCredentialCoordinatesUseCase(
            readTextFromImage = readTextFromImage,
            ghanaNhisCardOcrSelectorUseCase = ghanaNhisCardOcrSelectorUseCase,
            ghanaIdCardOcrSelectorUseCase = ghanaIdCardOcrSelectorUseCase,
            credentialImageRepository = credentialImageRepository,
        )
        coEvery { credentialImageRepository.saveCredentialScan(any(), any()) } returns savedImagePath
    }

    @Test
    fun `returns null when readTextFromImage returns null`() = runTest {
        every { readTextFromImage(bitmap) } returns null

        val result = useCase(bitmap, OcrDocumentType.NhisCard)

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when selector finds no matching line for NhisCard`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrSelectorUseCase(any()) } returns null

        val result = useCase(bitmap, OcrDocumentType.NhisCard)

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when selector finds no matching line for GhanaIdCard`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaIdCardOcrSelectorUseCase(any()) } returns null

        val result = useCase(bitmap, OcrDocumentType.GhanaIdCard)

        assertThat(result).isNull()
    }

    @Test
    fun `returns DetectedOcrBlock for NhisCard when line is found`() = runTest {
        val ocrLine = ocrLine(text = "12345678")
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrSelectorUseCase(any()) } returns ocrLine

        val result = useCase(bitmap, OcrDocumentType.NhisCard)

        assertThat(result).isNotNull()
        assertThat(result?.readoutValue).isEqualTo(ocrLine.text)
        assertThat(result?.lineBoundingBox).isEqualTo(ocrLine.boundingBox)
        assertThat(result?.blockBoundingBox).isEqualTo(ocrLine.blockBoundingBox)
        assertThat(result?.documentType).isEqualTo(OcrDocumentType.NhisCard)
    }

    @Test
    fun `returns DetectedOcrBlock for GhanaIdCard when line is found`() = runTest {
        val ocrLine = ocrLine(text = "GHA-123456789-0")
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaIdCardOcrSelectorUseCase(any()) } returns ocrLine

        val result = useCase(bitmap, OcrDocumentType.GhanaIdCard)

        assertThat(result).isNotNull()
        assertThat(result?.readoutValue).isEqualTo(ocrLine.text)
        assertThat(result?.documentType).isEqualTo(OcrDocumentType.GhanaIdCard)
    }

    @Test
    fun `saves image when line is found`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrSelectorUseCase(any()) } returns ocrLine()

        useCase(bitmap, OcrDocumentType.NhisCard)

        coVerify(exactly = 1) { credentialImageRepository.saveCredentialScan(bitmap, any()) }
    }

    @Test
    fun `does not save image when no line is found`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrSelectorUseCase(any()) } returns null

        useCase(bitmap, OcrDocumentType.NhisCard)

        coVerify(exactly = 0) { credentialImageRepository.saveCredentialScan(any(), any()) }
    }

    @Test
    fun `saved image path is set in result`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)
        every { ghanaNhisCardOcrSelectorUseCase(any()) } returns ocrLine()

        val result = useCase(bitmap, OcrDocumentType.NhisCard)

        assertThat(result?.imagePath).isEqualTo(savedImagePath)
    }

    @Test
    fun `returns null when exception is thrown`() = runTest {
        every { readTextFromImage(bitmap) } throws RuntimeException("OCR failed")

        val result = useCase(bitmap, OcrDocumentType.NhisCard)

        assertThat(result).isNull()
    }

    @Test
    fun `delegates NhisCard to nhis selector`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)

        useCase(bitmap, OcrDocumentType.NhisCard)

        coVerify(exactly = 1) { ghanaNhisCardOcrSelectorUseCase(any()) }
        coVerify(exactly = 0) { ghanaIdCardOcrSelectorUseCase(any()) }
    }

    @Test
    fun `delegates GhanaIdCard to ghana id selector`() = runTest {
        every { readTextFromImage(bitmap) } returns mockk(relaxed = true)

        useCase(bitmap, OcrDocumentType.GhanaIdCard)

        coVerify(exactly = 1) { ghanaIdCardOcrSelectorUseCase(any()) }
        coVerify(exactly = 0) { ghanaNhisCardOcrSelectorUseCase(any()) }
    }

    private fun ocrLine(text: String = "12345678") = OcrLine(
        id = 0,
        text = text,
        boundingBox = BoundingBox(left = 0, top = 100, right = 200, bottom = 130),
        blockBoundingBox = BoundingBox(left = 0, top = 90, right = 200, bottom = 140),
        confidence = 1f,
    )
}
