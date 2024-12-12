package com.simprints.fingerprint.infra.scanner.exceptions.unexpected

import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR

class UnknownScannerIssueException(
    message: String = "UnknownScannerIssueException",
) : UnexpectedScannerException(message) {
    companion object {
        fun forScannerError(scannerError: SCANNER_ERROR?) =
            UnknownScannerIssueException("For ScannerError: $scannerError - ${scannerError?.details()}")
    }
}
