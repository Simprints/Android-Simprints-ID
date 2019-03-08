package com.simprints.libscanner.tools.kotlin

import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.ScannerCallback

class WrapperScannerCallback(val success: () -> Unit,
                             val failure: (scannerError: SCANNER_ERROR?) -> Unit) : ScannerCallback {
    override fun onSuccess() {
        success()
    }

    override fun onFailure(error: SCANNER_ERROR?) {
        failure(error)
    }
}