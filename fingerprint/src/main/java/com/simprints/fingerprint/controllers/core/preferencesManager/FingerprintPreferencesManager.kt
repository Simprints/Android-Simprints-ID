package com.simprints.fingerprint.controllers.core.preferencesManager

import java.util.*

interface FingerprintPreferencesManager {
    var lastScannerUsed: String
    var lastScannerVersion: String
    var lastEnrolDate: Date?
    var lastVerificationDate: Date?
    var lastIdentificationDate: Date?
}
