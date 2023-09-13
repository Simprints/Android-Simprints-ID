package com.simprints.fingerprint.infra.scanner.exceptions.unexpected

import com.simprints.fingerprint.infra.scanner.exceptions.ScannerException
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR


open class UnexpectedScannerException() : ScannerException() {
    constructor(message: String) : this()
    constructor(throwable: Throwable) : this()

    companion object {

        @JvmStatic
        fun forScannerError(scannerError: SCANNER_ERROR?, where: String) =
            UnexpectedScannerException("Uncaught or invalid scanner reason in $where : ${scannerError?.details()}")
    }

}
