package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import java.util.*

interface FingerprintPreferencesManager {
    var lastScannerUsed: String
    var lastScannerVersion: String
    val fingerImagesExist: Boolean
    val saveFingerprintImages: SaveFingerprintImagesStrategy
    val scannerGenerations: List<ScannerGeneration>
}
