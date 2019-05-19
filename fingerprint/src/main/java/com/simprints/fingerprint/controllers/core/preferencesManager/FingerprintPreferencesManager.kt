package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.fingerprint.controllers.core.eventData.model.OneToManyMatchEvent

interface FingerprintPreferencesManager {
    var lastScannerUsed: String
    val matchPoolType: MatchPoolType
}
