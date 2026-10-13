package com.simprints.feature.externalcredential.screens.scandocument

import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadResult
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzData

/**
 * PoC: formats everything read from the document into plain text for display. Not localised on purpose.
 */
internal object DocumentScanResultFormatter {
    fun format(
        ocrMrz: MrzData,
        chip: ChipReadResult,
        currentYear: Int,
    ): String = buildString {
        val mrz = chip.mrz

        appendLine("DOCUMENT (DG1)")
        appendLine("Format: ${mrz.format}")
        appendLine("Document code: ${mrz.documentCode}")
        appendLine("Issuing state: ${mrz.issuingState}")
        appendLine("Document number: ${mrz.documentNumber}")
        appendLine("Surname: ${mrz.primaryIdentifier}")
        appendLine("Given names: ${mrz.secondaryIdentifier}")
        appendLine("Nationality: ${mrz.nationality}")
        appendLine("Date of birth: ${formatDateOfBirth(mrz.dateOfBirth, currentYear)}")
        appendLine("Sex: ${mrz.sex}")
        appendLine("Date of expiry: ${formatDateOfExpiry(mrz.dateOfExpiry)}")
        if (mrz.optionalData1.isNotEmpty()) appendLine("Optional data 1: ${mrz.optionalData1}")
        if (mrz.optionalData2.isNotEmpty()) appendLine("Optional data 2: ${mrz.optionalData2}")
        appendLine()

        appendLine("CHIP")
        appendLine("Access protocol: ${chip.accessProtocol}")
        appendLine("DG1 integrity (${chip.sodDigestAlgorithm} hash vs SOD): ${if (chip.dg1HashMatchesSod) "VALID" else "INVALID"}")
        appendLine("SOD signature: not verified")
        chip.documentSignerIssuer?.let { appendLine("Document signer issuer: $it") }
        chip.documentSignerSubject?.let { appendLine("Document signer subject: $it") }
        chip.ldsVersion?.let { appendLine("LDS version: $it") }
        chip.unicodeVersion?.let { appendLine("Unicode version: $it") }
        appendLine("Data groups (COM): ${chip.dataGroupsInCom?.formatDataGroups() ?: "not available"}")
        appendLine("Data groups (SOD): ${chip.dataGroupsInSod.formatDataGroups()}")
        appendLine()

        append("Camera MRZ matches chip: ${if (ocrMrz.matchesAccessKeyOf(mrz)) "YES" else "NO"}")

        if (chip.readLog.isNotEmpty()) {
            appendLine()
            appendLine()
            appendLine("CHIP READ LOG")
            append(chip.readLog.joinToString("\n"))
        }
    }

    private fun MrzData.matchesAccessKeyOf(other: MrzData) = documentNumber == other.documentNumber &&
        dateOfBirth == other.dateOfBirth &&
        dateOfExpiry == other.dateOfExpiry

    private fun List<Int>.formatDataGroups() = joinToString(", ") { "DG$it" }.ifEmpty { "none" }

    /** MRZ dates have 2-digit years, a date of birth cannot be in the future */
    private fun formatDateOfBirth(
        yymmdd: String,
        currentYear: Int,
    ): String {
        val year = yymmdd.take(2).toIntOrNull() ?: return yymmdd
        val century = if (year > currentYear % 100) 1900 else 2000
        return formatDate(yymmdd, century + year)
    }

    /** MRZ dates have 2-digit years, expiry dates of currently used documents are always in the 21st century */
    private fun formatDateOfExpiry(yymmdd: String): String {
        val year = yymmdd.take(2).toIntOrNull() ?: return yymmdd
        return formatDate(yymmdd, 2000 + year)
    }

    private fun formatDate(
        yymmdd: String,
        fullYear: Int,
    ) = if (yymmdd.length == 6) "$fullYear-${yymmdd.substring(2, 4)}-${yymmdd.substring(4, 6)}" else yymmdd
}
