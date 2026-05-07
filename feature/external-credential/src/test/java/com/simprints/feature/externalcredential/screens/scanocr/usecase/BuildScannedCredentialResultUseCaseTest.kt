package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ScannedMfidDocument
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.search.model.MfidDocument
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class BuildScannedCredentialResultUseCaseTest {
    @MockK
    private lateinit var buildMfidDocumentUseCase: BuildMfidDocumentUseCase

    @MockK
    private lateinit var createAndSaveZoomedImageUseCase: CreateAndSaveZoomedImageUseCase

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var useCase: BuildScannedCredentialResultUseCase

    private val startTime = Timestamp(100L)
    private val endTime = Timestamp(200L)
    private val documentImagePath = "path/to/document.jpg"
    private val zoomedImagePath = "path/to/zoomed.jpg"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = BuildScannedCredentialResultUseCase(
            buildMfidDocumentUseCase = buildMfidDocumentUseCase,
            createAndSaveZoomedImageUseCase = createAndSaveZoomedImageUseCase,
            timeHelper = timeHelper,
        )
        every { timeHelper.now() } returns endTime
        coEvery { createAndSaveZoomedImageUseCase(any(), any()) } returns zoomedImagePath
        every { buildMfidDocumentUseCase(any(), any()) } returns mockk<MfidDocument.GhanaNhisCard>(relaxed = true)
    }

    @Test
    fun `delegates document building to buildMfidDocumentUseCase`() = runTest {
        val documents = listOf(nhisScannedDocument())
        useCase(documents, OcrDocumentType.NhisCard, startTime)
        coVerify(exactly = 1) { buildMfidDocumentUseCase(documents, OcrDocumentType.NhisCard) }
    }

    @Test
    fun `result document is from buildMfidDocumentUseCase`() = runTest {
        val expectedDocument = mockk<MfidDocument.GhanaNhisCard>(relaxed = true)
        every { buildMfidDocumentUseCase(any(), any()) } returns expectedDocument
        val documents = listOf(nhisScannedDocument())

        val result = useCase(documents, OcrDocumentType.NhisCard, startTime)

        assertThat(result.document).isEqualTo(expectedDocument)
    }

    @Test
    fun `result scan start time matches provided start time`() = runTest {
        val documents = listOf(nhisScannedDocument())
        val result = useCase(documents, OcrDocumentType.NhisCard, startTime)
        assertThat(result.scanStartTime).isEqualTo(startTime)
    }

    @Test
    fun `result scan end time comes from timeHelper`() = runTest {
        val documents = listOf(nhisScannedDocument())
        val result = useCase(documents, OcrDocumentType.NhisCard, startTime)
        assertThat(result.scanEndTime).isEqualTo(endTime)
    }

    @Test
    fun `result document image path is taken from last scanned document`() = runTest {
        val lastFile = "last.jpg"
        val firstDocument = nhisScannedDocument(imagePath = "first.jpg")
        val lastDocument = nhisScannedDocument(imagePath = lastFile)

        val result = useCase(listOf(firstDocument, lastDocument), OcrDocumentType.NhisCard, startTime)

        assertThat(result.documentImagePath).isEqualTo(lastFile)
    }

    @Test
    fun `zoomed image is created using last document credential and image path`() = runTest {
        val lastFile = "last.jpg"
        val firstDocument = nhisScannedDocument(imagePath = "first.jpg")
        val lastDocument = nhisScannedDocument(imagePath = lastFile)

        useCase(listOf(firstDocument, lastDocument), OcrDocumentType.NhisCard, startTime)

        coVerify { createAndSaveZoomedImageUseCase(lastDocument.ocrScanResult.credential, lastFile) }
    }

    @Test
    fun `result zoomed image path comes from createAndSaveZoomedImageUseCase`() = runTest {
        val documents = listOf(nhisScannedDocument())
        val result = useCase(documents, OcrDocumentType.NhisCard, startTime)
        assertThat(result.zoomedCredentialImagePath).isEqualTo(zoomedImagePath)
    }

    @Test
    fun `result credential bounding box taken from last document credential block`() = runTest {
        val expectedBoundingBox = BoundingBox(left = 10, top = 20, right = 300, bottom = 50)
        val documents = listOf(nhisScannedDocument(credentialBlockBoundingBox = expectedBoundingBox))

        val result = useCase(documents, OcrDocumentType.NhisCard, startTime)

        assertThat(result.credentialBoundingBox).isEqualTo(expectedBoundingBox)
    }

    @Test
    fun `result zoomed image path is null when createAndSaveZoomedImageUseCase returns null`() = runTest {
        coEvery { createAndSaveZoomedImageUseCase(any(), any()) } returns null
        val documents = listOf(nhisScannedDocument())

        val result = useCase(documents, OcrDocumentType.NhisCard, startTime)

        assertThat(result.zoomedCredentialImagePath).isNull()
    }

    private fun ocrLine(
        text: String = "12345678",
        blockBoundingBox: BoundingBox? = null,
    ) = OcrLine(
        id = 0,
        text = text,
        boundingBox = BoundingBox(left = 0, top = 0, right = 100, bottom = 20),
        blockBoundingBox = blockBoundingBox ?: BoundingBox(left = 0, top = 0, right = 100, bottom = 20),
        confidence = 1f,
    )

    private fun nhisScannedDocument(
        imagePath: String = documentImagePath,
        credential: String = "12345678",
        credentialBlockBoundingBox: BoundingBox? = null,
    ) = ScannedMfidDocument(
        imagePath = imagePath,
        ocrScanResult = OcrScanResult.GhanaNhisCard(
            credential = ocrLine(credential, credentialBlockBoundingBox),
        ),
    )
}
