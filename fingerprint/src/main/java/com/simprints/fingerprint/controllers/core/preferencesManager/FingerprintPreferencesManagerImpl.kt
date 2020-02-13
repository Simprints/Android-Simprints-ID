package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.id.data.prefs.PreferencesManager

class FingerprintPreferencesManagerImpl(private val prefs: PreferencesManager): FingerprintPreferencesManager {

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

    override val fingerImagesExist: Boolean
        get() = prefs.fingerImagesExist

    override val saveFingerprintImages: Boolean
        get() = false // TODO : Delegate to core preferences manager
}
