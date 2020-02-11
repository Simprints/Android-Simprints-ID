package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.ScannerGeneration as IdScannerGeneration
import java.util.*
import com.simprints.id.data.prefs.settings.SaveFingerprintImagesStrategy as IdSaveFingerprintImagesStrategy

class FingerprintPreferencesManagerImpl(private val prefs: PreferencesManager) : FingerprintPreferencesManager {

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
            IdSaveFingerprintImagesStrategy.COMPRESSED_1700_WSQ_15 -> SaveFingerprintImagesStrategy.COMPRESSED_1700_WSQ_15
        }

    override val scannerGenerations: List<ScannerGeneration>
        get() = prefs.scannerGenerations.map {
            when (it) {
                IdScannerGeneration.VERO_1 -> ScannerGeneration.VERO_1
                IdScannerGeneration.VERO_2 -> ScannerGeneration.VERO_2
            }
        }
}
