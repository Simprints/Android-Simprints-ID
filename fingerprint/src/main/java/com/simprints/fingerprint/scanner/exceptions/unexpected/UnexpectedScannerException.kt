package com.simprints.fingerprint.scanner.exceptions.unexpected

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.fingerprintscanner.v1.SCANNER_ERROR


class UnexpectedScannerException(message: String = "UnexpectedScannerException") : FingerprintUnexpectedException(message) {

    companion object {

        @JvmStatic
        fun forScannerError(scannerError: SCANNER_ERROR?, where: String) =
            UnexpectedScannerException("Uncaught or invalid scanner reason in $where : ${scannerError?.details()}")
    }

}
