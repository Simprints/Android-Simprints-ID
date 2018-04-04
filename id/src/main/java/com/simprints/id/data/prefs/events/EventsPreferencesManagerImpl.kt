package com.simprints.id.data.prefs.events

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.tools.delegates.DatePreference
import com.simprints.id.tools.delegates.PrimitivePreference
import java.util.*

class EventsPreferencesManagerImpl(prefs: ImprovedSharedPreferences)
    : RecentEventsPreferencesManager {

    companion object {
        private const val LAST_SCANNER_USED_KEY = "LastScannerUsed"
        private const val LAST_SCANNER_USED_DEFAULT: String = ""

        private const val LAST_ENROL_EVENT_KEY = "LastEnrolEvent"
        private val LAST_ENROL_EVENT_DEFAULT = null

        private const val LAST_VERIFICATION_EVENT_KEY = "LastVerificationEvent"
        private val LAST_VERIFICATION_EVENT_DEFAULT = null

        private const val LAST_IDENTIFICATION_EVENT_KEY = "LastIdentificationEvent"
        private val LAST_IDENTIFICATION_EVENT_DEFAULT = null
    }

    override var lastScannerUsed: String
        by PrimitivePreference(prefs, LAST_SCANNER_USED_KEY, LAST_SCANNER_USED_DEFAULT)

    override var lastIdentificationDate: Date?
        by DatePreference(prefs, LAST_ENROL_EVENT_KEY, LAST_ENROL_EVENT_DEFAULT)

    override var lastEnrolDate: Date?
        by DatePreference(prefs, LAST_VERIFICATION_EVENT_KEY, LAST_VERIFICATION_EVENT_DEFAULT)

    override var lastVerificationDate: Date?
        by DatePreference(prefs, LAST_IDENTIFICATION_EVENT_KEY, LAST_IDENTIFICATION_EVENT_DEFAULT)
}
