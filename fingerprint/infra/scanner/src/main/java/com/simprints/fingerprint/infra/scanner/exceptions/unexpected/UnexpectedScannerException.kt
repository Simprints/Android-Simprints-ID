package com.simprints.fingerprint.infra.scanner.exceptions.unexpected

import com.simprints.fingerprint.infra.scanner.exceptions.ScannerException
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR

open class UnexpectedScannerException(
    message: String = "UnexpectedScannerException",
    throwable: Throwable? = null,
) : ScannerException(message, throwable) {
    companion object {
        @JvmStatic
        fun forScannerError(
            scannerError: SCANNER_ERROR?,
            where: String,
        ) = UnexpectedScannerException("Uncaught or invalid scanner reason in $where : ${scannerError?.details()}")
    }
}
