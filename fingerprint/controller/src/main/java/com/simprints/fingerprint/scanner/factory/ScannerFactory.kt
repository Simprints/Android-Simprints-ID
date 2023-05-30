package com.simprints.fingerprint.scanner.factory

import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper

interface ScannerFactory {

    suspend fun create(macAddress: String): ScannerWrapper
}
