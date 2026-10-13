package com.simprints.feature.externalcredential.screens.scandocument.model

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzData

internal enum class ChipAccessProtocol {
    PACE,
    BAC,
}

internal enum class ChipReadError {
    TagNotSupported,
    ConnectionLost,
    AccessDenied,
    ReadFailed,
}

/**
 * Data read from the document chip. Contains personal data - must be kept in memory only and never logged or persisted.
 *
 * @param mrz MRZ data stored on the chip in DG1
 * @param dataGroupsInCom data group numbers listed in EF.COM, or null if EF.COM could not be read
 * @param dataGroupsInSod data group numbers whose hashes are listed in EF.SOD
 * @param dg1HashMatchesSod whether the hash of the DG1 read from the chip matches the one signed in EF.SOD. Only the data integrity
 * is checked, the SOD signature and the certificate chain are not verified.
 * @param readLog diagnostic log of the read (protocol steps and timings only, access key redacted)
 */
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class ChipReadResult(
    val accessProtocol: ChipAccessProtocol,
    val ldsVersion: String?,
    val unicodeVersion: String?,
    val dataGroupsInCom: List<Int>?,
    val dataGroupsInSod: List<Int>,
    val sodDigestAlgorithm: String,
    val dg1HashMatchesSod: Boolean,
    val documentSignerSubject: String?,
    val documentSignerIssuer: String?,
    val mrz: MrzData,
    val readLog: List<String> = emptyList(),
) {
    // Prevents personal data from accidentally leaking into logs or crash reports
    override fun toString(): String = "ChipReadResult(accessProtocol=$accessProtocol, dg1HashMatchesSod=$dg1HashMatchesSod)"
}

/**
 * @param attempt number of the failed chip read attempt for the current MRZ, starting from 1
 * @param log diagnostic log of the failed attempt (protocol steps, status words and exceptions only, access key redacted)
 */
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class ChipReadFailure(
    val error: ChipReadError,
    val attempt: Int,
    val log: List<String>,
)

internal class ChipReadException(
    val error: ChipReadError,
    val log: List<String> = emptyList(),
    cause: Throwable? = null,
) : Exception(error.name, cause)
