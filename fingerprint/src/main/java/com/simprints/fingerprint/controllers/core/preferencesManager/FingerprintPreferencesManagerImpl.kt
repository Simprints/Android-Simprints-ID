package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.id.data.prefs.PreferencesManager

class FingerprintPreferencesManagerImpl(prefs: PreferencesManager,
                                        override var lastScannerUsed: String = prefs.lastScannerUsed): FingerprintPreferencesManager
