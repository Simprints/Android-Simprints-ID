package com.simprints.fingerprint.exceptions.unexpected.scanner

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.fingerprintscanner.SCANNER_ERROR

class UnknownScannerIssueException(message: String = "UnknownScannerIssueException") :
    FingerprintUnexpectedException(message) {

    companion object {
        fun forScannerError(scannerError: SCANNER_ERROR?) =
            UnknownScannerIssueException("For ScannerError: $scannerError - ${scannerError?.details()}")
    }
}
