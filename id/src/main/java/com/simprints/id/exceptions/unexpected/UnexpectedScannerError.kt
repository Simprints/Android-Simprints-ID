package com.simprints.id.exceptions.unexpected

import com.simprints.libscanner.SCANNER_ERROR


class UnexpectedScannerError(message: String = "UnexpectedScannerError") : UnexpectedException(message) {

    companion object {

        @JvmStatic
        fun forScannerError(scannerError: SCANNER_ERROR, where: String) =
                UnexpectedScannerError("Uncaught or invalid scanner error in $where : ${scannerError.details()}")
    }

}
