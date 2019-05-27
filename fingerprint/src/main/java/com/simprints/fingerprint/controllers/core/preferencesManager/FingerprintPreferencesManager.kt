package com.simprints.fingerprint.controllers.core.preferencesManager

import java.util.*

interface FingerprintPreferencesManager {
    var lastScannerUsed: String
    var lastScannerVersion: String
    val matchPoolType: MatchPoolType
    var lastEnrolDate: Date?
    var lastVerificationDate: Date?
    var lastIdentificationDate: Date?
}
