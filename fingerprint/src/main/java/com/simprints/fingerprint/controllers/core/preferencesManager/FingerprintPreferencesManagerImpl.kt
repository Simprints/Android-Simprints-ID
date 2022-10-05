package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.id.data.prefs.IdPreferencesManager

class FingerprintPreferencesManagerImpl(
    private val prefs: IdPreferencesManager,
) : FingerprintPreferencesManager {

    override var lastScannerUsed: String = prefs.lastScannerUsed
        set(value) {
            field = value
            prefs.lastScannerUsed = field
        }

    override var lastScannerVersion: String = prefs.lastScannerVersion
        set(value) {
            field = value
            prefs.lastScannerVersion = field
        }
}
