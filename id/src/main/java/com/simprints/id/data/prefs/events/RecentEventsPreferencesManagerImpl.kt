package com.simprints.id.data.prefs.events

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference

class RecentEventsPreferencesManagerImpl(prefs: ImprovedSharedPreferences)
    : RecentEventsPreferencesManager {

    companion object {
        private const val LAST_USER = "LastUserUsed"
        private const val LAST_USER_DEFAULT: String = ""

        private const val LAST_SCANNER_USED_KEY = "LastScannerUsed"
        private const val LAST_SCANNER_USED_DEFAULT: String = ""

        private const val LAST_MAC_ADDRESS_KEY = "LastMacAddress"
        private const val LAST_MAC_ADDRESS_DEFAULT: String = ""

        private const val ENROLMENTS_KEY = "Enrolments"
        private const val ENROLMENTS_DEFAULT = 0

        private const val IDENTIFICATIONS_KEY = "Identifications"
        private const val IDENTIFICATIONS_DEFAULT = 0

        private const val VERIFICATIONS_KEY = "Verifications"
        private const val VERIFICATIONS_DEFAULT = 0
    }

    override var lastScannerUsed: String
        by PrimitivePreference(prefs, LAST_SCANNER_USED_KEY, LAST_SCANNER_USED_DEFAULT)

    override var lastScannerVersion: String
        by PrimitivePreference(prefs, LAST_MAC_ADDRESS_KEY, LAST_MAC_ADDRESS_DEFAULT)

    override var lastUserUsed: String
        by PrimitivePreference(prefs, LAST_USER, LAST_USER_DEFAULT)

    override var enrolmentsToday: Int by PrimitivePreference(prefs, ENROLMENTS_KEY, ENROLMENTS_DEFAULT)

    override var identificationsToday: Int
        by PrimitivePreference(prefs, IDENTIFICATIONS_KEY, IDENTIFICATIONS_DEFAULT)

    override var verificationsToday: Int
        by PrimitivePreference(prefs, VERIFICATIONS_KEY, VERIFICATIONS_DEFAULT)

}
