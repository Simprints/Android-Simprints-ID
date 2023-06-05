package com.simprints.fingerprintscanner.v1

fun wrappedScannerCallback(onSuccess: () -> Unit, onFailure: (SCANNER_ERROR?) -> Unit): ScannerCallback =
        object : ScannerCallback {
            override fun onSuccess() {
                onSuccess()
            }

            override fun onFailure(error: SCANNER_ERROR?) {
                onFailure(error)
            }
        }
