package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy as IdCaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy as IdSaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration as IdScannerGeneration

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

    override val captureFingerprintStrategy: CaptureFingerprintStrategy
        get() = when (prefs.captureFingerprintStrategy) {
            IdCaptureFingerprintStrategy.SECUGEN_ISO_1700_DPI -> CaptureFingerprintStrategy.SECUGEN_ISO_1700_DPI
        }

    override val saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy
        get() = when (prefs.saveFingerprintImagesStrategy) {
            IdSaveFingerprintImagesStrategy.NEVER -> SaveFingerprintImagesStrategy.NEVER
            IdSaveFingerprintImagesStrategy.WSQ_15 -> SaveFingerprintImagesStrategy.WSQ_15
        }

    override val scannerGenerations: List<ScannerGeneration>
        get() = prefs.scannerGenerations.map {
            when (it) {
                IdScannerGeneration.VERO_1 -> ScannerGeneration.VERO_1
                IdScannerGeneration.VERO_2 -> ScannerGeneration.VERO_2
            }
        }
}
