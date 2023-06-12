package com.simprints.fingerprint.scanner.exceptions.unexpected

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR

class UnknownScannerIssueException(message: String = "UnknownScannerIssueException") :
    FingerprintUnexpectedException(message) {

    companion object {
        fun forScannerError(scannerError: SCANNER_ERROR?) =
            UnknownScannerIssueException("For ScannerError: $scannerError - ${scannerError?.details()}")
    }
}
