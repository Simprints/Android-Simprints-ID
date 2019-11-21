package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.id.data.prefs.PreferencesManager
import java.util.*

class FingerprintPreferencesManagerImpl(private val prefs: PreferencesManager): FingerprintPreferencesManager {

    override var lastVerificationDate: Date? = prefs.lastVerificationDate
        set(value) {
            field = value
            prefs.lastVerificationDate = field
        }

    override var lastIdentificationDate: Date? = prefs.lastIdentificationDate
        set(value) {
            field = value
            prefs.lastIdentificationDate = field
        }

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

    override val saveImages: Boolean
        get() = true // TODO : Delegate to core preferences manager
}
