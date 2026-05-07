package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ScannedMfidDocument
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.search.model.MfidDocument
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class BuildMfidDocumentUseCaseTest {
    private val getBestReadout = mockk<GetBestReadoutBasedOnConfidenceUseCase>()

    private lateinit var useCase: BuildMfidDocumentUseCase

    @Before
    fun setUp() {
        every { getBestReadout(any(), any()) } answers {
            firstArg<List<String>>().first()
        }
        useCase = BuildMfidDocumentUseCase(getBestReadout)
    }

    @Test
    fun `returns GhanaNhisCard for NhisCard document type`() {
        val documents = listOf(nhisScannedDocument())
        val result = useCase(documents, OcrDocumentType.NhisCard)
        assertThat(result).isInstanceOf(MfidDocument.GhanaNhisCard::class.java)
    }

    @Test
    fun `returns GhanaIdCard for GhanaIdCard document type`() {
        val documents = listOf(ghanaIdScannedDocument())
        val result = useCase(documents, OcrDocumentType.GhanaIdCard)
        assertThat(result).isInstanceOf(MfidDocument.GhanaIdCard::class.java)
    }

    @Test
    fun `passes credential texts with nhis target length to best readout use case`() {
        val credentialText = "12345678"
        val documents = listOf(nhisScannedDocument(credential = credentialText))

        useCase(documents, OcrDocumentType.NhisCard)

        verify { getBestReadout(listOf(credentialText), targetLength = NHIS_CREDENTIAL_LENGTH) }
    }

    @Test
    fun `passes credential texts with ghana id target length to best readout use case`() {
        val credentialText = "GHA-123456789-0"
        val documents = listOf(ghanaIdScannedDocument(credential = credentialText))

        useCase(documents, OcrDocumentType.GhanaIdCard)

        verify { getBestReadout(listOf(credentialText), targetLength = GHANA_ID_CREDENTIAL_LENGTH) }
    }

    @Test
    fun `sets credential from best readout for NhisCard`() {
        val expectedCredential = "12345678"
        every { getBestReadout(listOf(expectedCredential), targetLength = NHIS_CREDENTIAL_LENGTH) } returns expectedCredential
        val documents = listOf(nhisScannedDocument(credential = expectedCredential))

        val result = useCase(documents, OcrDocumentType.NhisCard) as MfidDocument.GhanaNhisCard

        assertThat(result.credential).isEqualTo(expectedCredential.asTokenizableRaw())
    }

    @Test
    fun `sets credential from best readout for GhanaIdCard`() {
        val expectedCredential = "GHA-123456789-0"
        every { getBestReadout(listOf(expectedCredential), targetLength = GHANA_ID_CREDENTIAL_LENGTH) } returns expectedCredential
        val documents = listOf(ghanaIdScannedDocument(credential = expectedCredential))

        val result = useCase(documents, OcrDocumentType.GhanaIdCard) as MfidDocument.GhanaIdCard

        assertThat(result.credential).isEqualTo(expectedCredential.asTokenizableRaw())
    }

    @Test
    fun `maps name field from nhis scan result`() {
        val nameText = "JOHN DOE"
        val documents = listOf(nhisScannedDocument(name = nameText))

        val result = useCase(documents, OcrDocumentType.NhisCard) as MfidDocument.GhanaNhisCard

        assertThat(result.name).isEqualTo(nameText.asTokenizableRaw())
    }

    @Test
    fun `maps dateOfBirth field from nhis scan result`() {
        val dob = "01/01/1990"
        val documents = listOf(nhisScannedDocument(dateOfBirth = dob))

        val result = useCase(documents, OcrDocumentType.NhisCard) as MfidDocument.GhanaNhisCard

        assertThat(result.dateOfBirth).isEqualTo(dob.asTokenizableRaw())
    }

    @Test
    fun `maps sex field from nhis scan result`() {
        val sex = "M"
        val documents = listOf(nhisScannedDocument(sex = sex))

        val result = useCase(documents, OcrDocumentType.NhisCard) as MfidDocument.GhanaNhisCard

        assertThat(result.sex).isEqualTo(sex.asTokenizableRaw())
    }

    @Test
    fun `maps dateOfIssue field from nhis scan result`() {
        val dateOfIssue = "01/01/2020"
        val documents = listOf(nhisScannedDocument(dateOfIssue = dateOfIssue))

        val result = useCase(documents, OcrDocumentType.NhisCard) as MfidDocument.GhanaNhisCard

        assertThat(result.dateOfIssue).isEqualTo(dateOfIssue.asTokenizableRaw())
    }

    @Test
    fun `returns null non-credential fields when nhis scan result has no fields`() {
        val documents = listOf(nhisScannedDocument())

        val result = useCase(documents, OcrDocumentType.NhisCard) as MfidDocument.GhanaNhisCard

        assertThat(result.name).isNull()
        assertThat(result.dateOfBirth).isNull()
        assertThat(result.sex).isNull()
        assertThat(result.dateOfIssue).isNull()
    }

    @Test
    fun `maps surname field from ghana id scan result`() {
        val surname = "DOE"
        val documents = listOf(ghanaIdScannedDocument(surname = surname))

        val result = useCase(documents, OcrDocumentType.GhanaIdCard) as MfidDocument.GhanaIdCard

        assertThat(result.surname).isEqualTo(surname.asTokenizableRaw())
    }

    @Test
    fun `maps firstName field from ghana id scan result`() {
        val firstName = "JOHN"
        val documents = listOf(ghanaIdScannedDocument(firstName = firstName))

        val result = useCase(documents, OcrDocumentType.GhanaIdCard) as MfidDocument.GhanaIdCard

        assertThat(result.firstName).isEqualTo(firstName.asTokenizableRaw())
    }

    @Test
    fun `returns null non-credential fields when ghana id scan result has no fields`() {
        val documents = listOf(ghanaIdScannedDocument())

        val result = useCase(documents, OcrDocumentType.GhanaIdCard) as MfidDocument.GhanaIdCard

        assertThat(result.surname).isNull()
        assertThat(result.firstName).isNull()
        assertThat(result.nationality).isNull()
        assertThat(result.dateOfBirth).isNull()
        assertThat(result.height).isNull()
        assertThat(result.documentNumber).isNull()
        assertThat(result.placeOfIssue).isNull()
        assertThat(result.dateOfIssue).isNull()
        assertThat(result.dateOfExpiry).isNull()
    }

    @Test
    fun `ignores non-matching scan result types when building nhis card fields`() {
        val expectedName = "JANE DOE"
        val nhisDocument = nhisScannedDocument(name = expectedName)
        val ghanaIdDocument = ghanaIdScannedDocument()
        val documents = listOf(nhisDocument, ghanaIdDocument)

        val result = useCase(documents, OcrDocumentType.NhisCard) as MfidDocument.GhanaNhisCard

        assertThat(result.name).isEqualTo(expectedName.asTokenizableRaw())
    }

    @Test
    fun `aggregates credentials from multiple scans via best readout use case`() {
        val bestReadout = "12345678"
        val texts = listOf(bestReadout, "12345679", "12345678")
        val documents = texts.map { nhisScannedDocument(credential = it) }
        every { getBestReadout(texts, targetLength = NHIS_CREDENTIAL_LENGTH) } returns bestReadout

        val result = useCase(documents, OcrDocumentType.NhisCard) as MfidDocument.GhanaNhisCard

        assertThat(result.credential).isEqualTo(bestReadout.asTokenizableRaw())
        verify { getBestReadout(texts, targetLength = NHIS_CREDENTIAL_LENGTH) }
    }

    private fun ocrLine(text: String) = OcrLine(
        id = 0,
        text = text,
        boundingBox = BoundingBox(left = 0, top = 0, right = 100, bottom = 20),
        blockBoundingBox = BoundingBox(left = 0, top = 0, right = 100, bottom = 20),
        confidence = 1f,
    )

    private fun nhisScannedDocument(
        credential: String = "12345678",
        name: String? = null,
        dateOfBirth: String? = null,
        sex: String? = null,
        dateOfIssue: String? = null,
    ) = ScannedMfidDocument(
        imagePath = "path/to/image.jpg",
        ocrScanResult = OcrScanResult.GhanaNhisCard(
            credential = ocrLine(credential),
            name = name?.let { ocrLine(it) },
            dateOfBirth = dateOfBirth?.let { ocrLine(it) },
            sex = sex?.let { ocrLine(it) },
            dateOfIssue = dateOfIssue?.let { ocrLine(it) },
        ),
    )

    private fun ghanaIdScannedDocument(
        credential: String = "GHA-123456789-0",
        surname: String? = null,
        firstName: String? = null,
        nationality: String? = null,
        dateOfBirth: String? = null,
        height: String? = null,
        documentNumber: String? = null,
        placeOfIssue: String? = null,
        dateOfIssue: String? = null,
        dateOfExpiry: String? = null,
    ) = ScannedMfidDocument(
        imagePath = "path/to/image.jpg",
        ocrScanResult = OcrScanResult.GhanaIdCard(
            credential = ocrLine(credential),
            surname = surname?.let { ocrLine(it) },
            firstName = firstName?.let { ocrLine(it) },
            nationality = nationality?.let { ocrLine(it) },
            dateOfBirth = dateOfBirth?.let { ocrLine(it) },
            height = height?.let { ocrLine(it) },
            documentNumber = documentNumber?.let { ocrLine(it) },
            placeOfIssue = placeOfIssue?.let { ocrLine(it) },
            dateOfIssue = dateOfIssue?.let { ocrLine(it) },
            dateOfExpiry = dateOfExpiry?.let { ocrLine(it) },
        ),
    )

    companion object {
        private const val NHIS_CREDENTIAL_LENGTH = 8
        private const val GHANA_ID_CREDENTIAL_LENGTH = 15
    }
}
