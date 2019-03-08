package com.simprints.id.exceptions.unexpected

import com.simprints.fingerprintscanner.SCANNER_ERROR


class UnexpectedScannerException(message: String = "UnexpectedScannerException") : UnexpectedException(message) {

    companion object {

        @JvmStatic
        fun forScannerError(scannerError: SCANNER_ERROR, where: String) =
                UnexpectedScannerException("Uncaught or invalid scanner error in $where : ${scannerError.details()}")
    }

}
