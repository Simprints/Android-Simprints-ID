package com.simprints.fingerprint.controllers.core.preferencesManager

interface FingerprintPreferencesManager {
    var lastScannerUsed: String
    var lastScannerVersion: String
    val fingerImagesExist: Boolean
}
