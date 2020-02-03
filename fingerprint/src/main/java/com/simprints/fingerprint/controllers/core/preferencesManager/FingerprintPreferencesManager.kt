package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.fingerprint.data.domain.fingerprint.SaveFingerprintImagesStrategy
import java.util.*

interface FingerprintPreferencesManager {
    var lastScannerUsed: String
    var lastScannerVersion: String
    var lastVerificationDate: Date?
    var lastIdentificationDate: Date?

    val fingerImagesExist: Boolean
    val saveFingerprintImages: SaveFingerprintImagesStrategy
}
