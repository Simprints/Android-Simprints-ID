package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.fingerprint.data.domain.fingerprint.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.PreferencesManager
import java.util.*
import com.simprints.id.data.prefs.settings.SaveFingerprintImagesStrategy as IdSaveFingerprintImagesStrategy

class FingerprintPreferencesManagerImpl(private val prefs: PreferencesManager) : FingerprintPreferencesManager {

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

    override val saveFingerprintImages: SaveFingerprintImagesStrategy
        get() = when (prefs.saveFingerprintImages) {
            IdSaveFingerprintImagesStrategy.NEVER -> SaveFingerprintImagesStrategy.NEVER
            IdSaveFingerprintImagesStrategy.ALWAYS -> SaveFingerprintImagesStrategy.ALWAYS
        }
}
