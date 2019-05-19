package com.simprints.fingerprint.controllers.core.preferencesManager

import com.simprints.fingerprint.controllers.core.preferencesManager.MatchPoolType.Companion.fromConstantGroup
import com.simprints.id.data.prefs.PreferencesManager

class FingerprintPreferencesManagerImpl(prefs: PreferencesManager,
                                        override var lastScannerUsed: String = prefs.lastScannerUsed,
                                        override val matchPoolType: MatchPoolType = fromConstantGroup(prefs.matchGroup)): FingerprintPreferencesManager
