package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.domain.ScannerGeneration

interface FingerprintPreferencesManager {
    var lastScannerUsed: String
    var lastScannerVersion: String
    val fingerImagesExist: Boolean
    val captureFingerprintStrategy: CaptureFingerprintStrategy
    val saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy
    val scannerGenerations: List<ScannerGeneration>
}
