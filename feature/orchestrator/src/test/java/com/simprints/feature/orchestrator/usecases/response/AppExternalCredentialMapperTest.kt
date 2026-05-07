package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.screens.search.model.MfidDocument
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import org.junit.Test

internal class AppExternalCredentialMapperTest {
    @Test
    fun `returns null when search result is null`() {
        val result = (null as ExternalCredentialSearchResult.Complete?).toAppExternalCredential()

        assertThat(result).isNull()
    }

    @Test
    fun `maps scan id as credential id`() {
        val scanId = "test-scan-id"
        val searchResult = makeSearchResult(
            document = nhisCard(),
            credentialScanId = scanId,
        )

        val result = searchResult.toAppExternalCredential()

        assertThat(result?.id).isEqualTo(scanId)
    }

    @Test
    fun `maps confirmed credential as value`() {
        val confirmedCredential = "12345678".asTokenizableRaw()
        val searchResult = makeSearchResult(
            document = nhisCard(),
            confirmedCredential = confirmedCredential,
        )

        val result = searchResult.toAppExternalCredential()

        assertThat(result?.value).isEqualTo(confirmedCredential)
    }

    @Test
    fun `maps nhis card credential type`() {
        val searchResult = makeSearchResult(document = nhisCard())
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.type).isEqualTo(ExternalCredentialType.NHISCard)
    }

    @Test
    fun `maps ghana id card credential type`() {
        val searchResult = makeSearchResult(document = ghanaIdCard())
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.type).isEqualTo(ExternalCredentialType.GhanaIdCard)
    }

    @Test
    fun `maps qr code credential type`() {
        val searchResult = makeSearchResult(document = qrCode())
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.type).isEqualTo(ExternalCredentialType.QRCode)
    }

    @Test
    fun `maps nhis card name field`() {
        val searchResult = makeSearchResult(document = nhisCard(name = "JOHN DOE"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("name", "JOHN DOE")
    }

    @Test
    fun `maps nhis card dateOfBirth field`() {
        val searchResult = makeSearchResult(document = nhisCard(dateOfBirth = "01/01/1990"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("dateOfBirth", "01/01/1990")
    }

    @Test
    fun `maps nhis card sex field`() {
        val searchResult = makeSearchResult(document = nhisCard(sex = "M"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("sex", "M")
    }

    @Test
    fun `maps nhis card dateOfIssue field`() {
        val searchResult = makeSearchResult(document = nhisCard(dateOfIssue = "01/01/2020"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("dateOfIssue", "01/01/2020")
    }

    @Test
    fun `omits null nhis card fields from non-credential fields`() {
        val searchResult = makeSearchResult(document = nhisCard())
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).isEmpty()
    }

    @Test
    fun `maps all present nhis card fields`() {
        val searchResult = makeSearchResult(
            document = nhisCard(
                name = "JOHN DOE",
                dateOfBirth = "01/01/1990",
                sex = "M",
                dateOfIssue = "01/01/2020",
            ),
        )

        val result = searchResult.toAppExternalCredential()

        assertThat(result?.nonCredentialFields).containsExactly(
            "name",
            "JOHN DOE",
            "dateOfBirth",
            "01/01/1990",
            "sex",
            "M",
            "dateOfIssue",
            "01/01/2020",
        )
    }

    @Test
    fun `maps ghana id card surname field`() {
        val searchResult = makeSearchResult(document = ghanaIdCard(surname = "DOE"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("surname", "DOE")
    }

    @Test
    fun `maps ghana id card firstName field`() {
        val searchResult = makeSearchResult(document = ghanaIdCard(firstName = "JOHN"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("firstName", "JOHN")
    }

    @Test
    fun `maps ghana id card nationality field`() {
        val searchResult = makeSearchResult(document = ghanaIdCard(nationality = "GHANAIAN"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("nationality", "GHANAIAN")
    }

    @Test
    fun `maps ghana id card dateOfBirth field`() {
        val searchResult = makeSearchResult(document = ghanaIdCard(dateOfBirth = "01/01/1990"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("dateOfBirth", "01/01/1990")
    }

    @Test
    fun `maps ghana id card height field`() {
        val searchResult = makeSearchResult(document = ghanaIdCard(height = "1.75"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("height", "1.75")
    }

    @Test
    fun `maps ghana id card documentNumber field`() {
        val searchResult = makeSearchResult(document = ghanaIdCard(documentNumber = "123456"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("documentNumber", "123456")
    }

    @Test
    fun `maps ghana id card placeOfIssue field`() {
        val searchResult = makeSearchResult(document = ghanaIdCard(placeOfIssue = "ACCRA"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("placeOfIssue", "ACCRA")
    }

    @Test
    fun `maps ghana id card dateOfIssue field`() {
        val searchResult = makeSearchResult(document = ghanaIdCard(dateOfIssue = "01/01/2020"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("dateOfIssue", "01/01/2020")
    }

    @Test
    fun `maps ghana id card dateOfExpiry field`() {
        val searchResult = makeSearchResult(document = ghanaIdCard(dateOfExpiry = "01/01/2030"))
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).containsEntry("dateOfExpiry", "01/01/2030")
    }

    @Test
    fun `omits null ghana id card fields from non-credential fields`() {
        val searchResult = makeSearchResult(document = ghanaIdCard())
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).isEmpty()
    }

    @Test
    fun `qr code has empty non-credential fields`() {
        val searchResult = makeSearchResult(document = qrCode())
        val result = searchResult.toAppExternalCredential()
        assertThat(result?.nonCredentialFields).isEmpty()
    }

    private fun makeSearchResult(
        document: MfidDocument,
        credentialScanId: String = "scan-id",
        confirmedCredential: com.simprints.core.domain.tokenization.TokenizableString.Raw = "12345678".asTokenizableRaw(),
    ) = ExternalCredentialSearchResult.Complete(
        flowType = FlowType.ENROL,
        scannedCredentialResult = ScannedCredentialResult(
            credentialScanId = credentialScanId,
            document = document,
            documentImagePath = null,
            zoomedCredentialImagePath = null,
            credentialBoundingBox = null,
            scanStartTime = Timestamp(0L),
            scanEndTime = Timestamp(1L),
        ),
        confirmedCredential = confirmedCredential,
        matchResults = emptyList(),
    )

    private fun nhisCard(
        credential: String = "12345678",
        name: String? = null,
        dateOfBirth: String? = null,
        sex: String? = null,
        dateOfIssue: String? = null,
    ) = MfidDocument.GhanaNhisCard(
        credential = credential.asTokenizableRaw(),
        name = name?.asTokenizableRaw(),
        dateOfBirth = dateOfBirth?.asTokenizableRaw(),
        sex = sex?.asTokenizableRaw(),
        dateOfIssue = dateOfIssue?.asTokenizableRaw(),
    )

    private fun ghanaIdCard(
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
    ) = MfidDocument.GhanaIdCard(
        credential = credential.asTokenizableRaw(),
        surname = surname?.asTokenizableRaw(),
        firstName = firstName?.asTokenizableRaw(),
        nationality = nationality?.asTokenizableRaw(),
        dateOfBirth = dateOfBirth?.asTokenizableRaw(),
        height = height?.asTokenizableRaw(),
        documentNumber = documentNumber?.asTokenizableRaw(),
        placeOfIssue = placeOfIssue?.asTokenizableRaw(),
        dateOfIssue = dateOfIssue?.asTokenizableRaw(),
        dateOfExpiry = dateOfExpiry?.asTokenizableRaw(),
    )

    private fun qrCode(credential: String = "qr-code-value") = MfidDocument.GhanaQrCode(
        credential = credential.asTokenizableRaw(),
    )
}
