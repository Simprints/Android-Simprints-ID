package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.id.data.prefs.PreferencesManager
import java.util.*

class FingerprintPreferencesManagerImpl(private val prefs: PreferencesManager): FingerprintPreferencesManager {

    override var lastEnrolDate: Date? = prefs.lastEnrolDate
        set(value) {
            field = value
            prefs.lastEnrolDate = field
        }

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
}
