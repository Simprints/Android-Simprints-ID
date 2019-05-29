package com.simprints.fingerprint.controllers.core.analytics

interface FingerprintAnalyticsManager {
    fun logScannerProperties(macAddress: String, scannerId: String)
}
