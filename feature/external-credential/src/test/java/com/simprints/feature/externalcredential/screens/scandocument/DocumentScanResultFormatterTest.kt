package com.simprints.feature.externalcredential.screens.scandocument

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipAccessProtocol
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadResult
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzData
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzFormat
import org.junit.Test

internal class DocumentScanResultFormatterTest {
    @Test
    fun `formats all chip data`() {
        val text = DocumentScanResultFormatter.format(OCR_MRZ, CHIP_RESULT, currentYear = 2026)

        assertThat(text).contains("Format: TD3")
        assertThat(text).contains("Document code: P<")
        assertThat(text).contains("Issuing state: UTO")
        assertThat(text).contains("Document number: L898902C3")
        assertThat(text).contains("Surname: ERIKSSON")
        assertThat(text).contains("Given names: ANNA MARIA")
        assertThat(text).contains("Nationality: UTO")
        assertThat(text).contains("Sex: F")
        assertThat(text).contains("Optional data 1: ZE184226B")
        assertThat(text).doesNotContain("Optional data 2")
        assertThat(text).contains("Access protocol: BAC")
        assertThat(text).contains("DG1 integrity (SHA-256 hash vs SOD): VALID")
        assertThat(text).contains("Document signer issuer: CN=CSCA")
        assertThat(text).contains("Document signer subject: CN=DS")
        assertThat(text).contains("LDS version: 0107")
        assertThat(text).contains("Data groups (COM): DG1, DG2, DG14")
        assertThat(text).contains("Data groups (SOD): DG1, DG2")
        assertThat(text).contains("Camera MRZ matches chip: YES")
    }

    @Test
    fun `formats MRZ dates with full years`() {
        val text = DocumentScanResultFormatter.format(OCR_MRZ, CHIP_RESULT, currentYear = 2026)

        assertThat(text).contains("Date of birth: 1974-08-12")
        assertThat(text).contains("Date of expiry: 2012-04-15")
    }

    @Test
    fun `date of birth in this century is not placed in the future`() {
        val chip = CHIP_RESULT.copy(mrz = CHIP_RESULT.mrz.copy(dateOfBirth = "150101"))

        val text = DocumentScanResultFormatter.format(OCR_MRZ, chip, currentYear = 2026)

        assertThat(text).contains("Date of birth: 2015-01-01")
    }

    @Test
    fun `reports invalid DG1 hash and missing COM`() {
        val chip = CHIP_RESULT.copy(dg1HashMatchesSod = false, dataGroupsInCom = null)

        val text = DocumentScanResultFormatter.format(OCR_MRZ, chip, currentYear = 2026)

        assertThat(text).contains("DG1 integrity (SHA-256 hash vs SOD): INVALID")
        assertThat(text).contains("Data groups (COM): not available")
    }

    @Test
    fun `reports mismatch between camera and chip MRZ`() {
        val text = DocumentScanResultFormatter.format(OCR_MRZ.copy(dateOfExpiry = "130415"), CHIP_RESULT, currentYear = 2026)

        assertThat(text).contains("Camera MRZ matches chip: NO")
    }

    @Test
    fun `appends the chip read log when available`() {
        val chip = CHIP_RESULT.copy(readLog = listOf("Connected to the chip", "Access granted (BAC)"))

        val text = DocumentScanResultFormatter.format(OCR_MRZ, chip, currentYear = 2026)

        assertThat(text).endsWith("CHIP READ LOG\nConnected to the chip\nAccess granted (BAC)")
    }

    @Test
    fun `omits the chip read log section when empty`() {
        val text = DocumentScanResultFormatter.format(OCR_MRZ, CHIP_RESULT, currentYear = 2026)

        assertThat(text).doesNotContain("CHIP READ LOG")
    }

    companion object {
        private val OCR_MRZ = MrzData(
            format = MrzFormat.TD3,
            documentCode = "P<",
            issuingState = "UTO",
            documentNumber = "L898902C3",
            dateOfBirth = "740812",
            sex = "F",
            dateOfExpiry = "120415",
            nationality = "UTO",
            primaryIdentifier = "ERIKSSON",
            secondaryIdentifier = "ANNA MARIA",
            optionalData1 = "ZE184226B",
            optionalData2 = "",
        )

        private val CHIP_RESULT = ChipReadResult(
            accessProtocol = ChipAccessProtocol.BAC,
            ldsVersion = "0107",
            unicodeVersion = "040000",
            dataGroupsInCom = listOf(1, 2, 14),
            dataGroupsInSod = listOf(1, 2),
            sodDigestAlgorithm = "SHA-256",
            dg1HashMatchesSod = true,
            documentSignerSubject = "CN=DS",
            documentSignerIssuer = "CN=CSCA",
            mrz = OCR_MRZ,
        )
    }
}
